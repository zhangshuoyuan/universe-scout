package cn.yuan.scout.file.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.yuan.scout.common.exception.BizException;
import cn.yuan.scout.common.exception.ErrorCode;
import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.file.config.UniverseFileProperties;
import cn.yuan.scout.file.dto.ImageFilePageItemResponse;
import cn.yuan.scout.file.dto.UploadBatchDetailResponse;
import cn.yuan.scout.file.dto.UploadBatchListItemResponse;
import cn.yuan.scout.file.dto.UploadFileItemResponse;
import cn.yuan.scout.file.dto.UploadResponse;
import cn.yuan.scout.file.entity.UploadBatchEntity;
import cn.yuan.scout.file.entity.UploadFileEntity;
import cn.yuan.scout.file.service.FileStorageService;
import cn.yuan.scout.file.service.UploadBatchService;
import cn.yuan.scout.file.service.UploadFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final DateTimeFormatter BATCH_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UniverseFileProperties fileProperties;
    private final UploadBatchService uploadBatchService;
    private final UploadFileService uploadFileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "please select at least one image");
        }

        List<MultipartFile> validFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                validateImageFile(file.getOriginalFilename());
                validFiles.add(file);
            }
        }

        if (validFiles.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "please select at least one valid image");
        }

        UploadBatchEntity batch = createBatch("IMAGE_BATCH", "Image upload batch");
        List<UploadFileEntity> fileEntities = new ArrayList<>();
        for (MultipartFile file : validFiles) {
            fileEntities.add(saveMultipartImage(batch.getId(), file, baseUploadPath()));
        }
        uploadFileService.saveBatch(fileEntities);
        finishBatch(batch, fileEntities.size());

        return buildUploadResponse(batch, fileEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResponse uploadZip(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "please select a zip file");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!"zip".equals(extension)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "file must be a zip package");
        }

        UploadBatchEntity batch = createBatch("ZIP", "Zip upload batch");
        saveOriginalZip(file, batch.getBatchNo());
        Path batchExtractDir = dateDirectory(baseExtractPath()).resolve(batch.getBatchNo());
        createDirectories(batchExtractDir);

        List<UploadFileEntity> fileEntities = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String originalName = Path.of(entry.getName()).getFileName().toString();
                String imageExtension = getExtension(originalName);
                if (!IMAGE_EXTENSIONS.contains(imageExtension)) {
                    continue;
                }

                String storedFileName = IdUtil.fastSimpleUUID() + "." + imageExtension;
                Path targetPath = batchExtractDir.resolve(storedFileName).normalize();
                if (!targetPath.startsWith(batchExtractDir)) {
                    throw new BizException(ErrorCode.PARAM_ERROR, "invalid zip entry path");
                }
                Files.copy(zipInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                fileEntities.add(buildUploadFile(batch.getId(), originalName, storedFileName, targetPath, imageExtension));
            }
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "failed to extract zip file", e);
        }

        if (fileEntities.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "zip package contains no valid image");
        }

        uploadFileService.saveBatch(fileEntities);
        finishBatch(batch, fileEntities.size());

        return buildUploadResponse(batch, fileEntities);
    }

    @Override
    public PageResult<UploadBatchListItemResponse> pageBatches(Long pageNum, Long pageSize, String sourceType, String status) {
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<UploadBatchEntity> wrapper = new LambdaQueryWrapper<UploadBatchEntity>()
                .eq(UploadBatchEntity::getDeleted, 0)
                .eq(StringUtils.hasText(sourceType), UploadBatchEntity::getSourceType, sourceType)
                .eq(StringUtils.hasText(status), UploadBatchEntity::getStatus, status)
                .orderByDesc(UploadBatchEntity::getCreatedAt);

        Page<UploadBatchEntity> page = uploadBatchService.page(new Page<>(safePageNum, safePageSize), wrapper);
        List<UploadBatchListItemResponse> records = page.getRecords()
                .stream()
                .map(this::toBatchListItem)
                .toList();

        return PageResult.of(records, page.getTotal(), safePageNum, safePageSize);
    }

    @Override
    public UploadBatchDetailResponse getBatchDetail(Long batchId) {
        UploadBatchEntity batch = uploadBatchService.getOne(
                new LambdaQueryWrapper<UploadBatchEntity>()
                        .eq(UploadBatchEntity::getId, batchId)
                        .eq(UploadBatchEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (batch == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "upload batch does not exist");
        }

        List<UploadFileItemResponse> files = uploadFileService.list(
                        new LambdaQueryWrapper<UploadFileEntity>()
                                .eq(UploadFileEntity::getBatchId, batchId)
                                .eq(UploadFileEntity::getDeleted, 0)
                                .orderByAsc(UploadFileEntity::getCreatedAt)
                )
                .stream()
                .map(this::toFileItem)
                .toList();

        return UploadBatchDetailResponse.builder()
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .batchName(batch.getBatchName())
                .sourceType(batch.getSourceType())
                .status(batch.getStatus())
                .totalFiles(batch.getTotalFiles())
                .successCount(batch.getSuccessCount())
                .failedCount(batch.getFailedCount())
                .remark(batch.getRemark())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .files(files)
                .build();
    }

    @Override
    public PageResult<ImageFilePageItemResponse> pageImages(Long pageNum, Long pageSize, Long batchId, String fileType, String keyword) {
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 100);

        LambdaQueryWrapper<UploadFileEntity> wrapper = new LambdaQueryWrapper<UploadFileEntity>()
                .eq(UploadFileEntity::getDeleted, 0)
                .eq(batchId != null, UploadFileEntity::getBatchId, batchId)
                .eq(StringUtils.hasText(fileType), UploadFileEntity::getFileType, normalizeFileType(fileType))
                .like(StringUtils.hasText(keyword), UploadFileEntity::getOriginalFilename, keyword)
                .orderByDesc(UploadFileEntity::getCreatedAt);

        Page<UploadFileEntity> page = uploadFileService.page(new Page<>(safePageNum, safePageSize), wrapper);
        List<ImageFilePageItemResponse> records = page.getRecords()
                .stream()
                .map(this::toImagePageItem)
                .toList();
        return PageResult.of(records, page.getTotal(), safePageNum, safePageSize);
    }

    @Override
    public ResponseEntity<Resource> previewImage(Long fileId) {
        UploadFileEntity file = getActiveFile(fileId);
        Path path = Path.of(file.getFilePath()).normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "image file does not exist on disk");
        }

        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(mediaType(file.getFileType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long fileId) {
        UploadFileEntity file = getActiveFile(fileId);
        file.setDeleted(1);
        file.setStatus("FAILED");
        file.setErrorMessage("deleted by admin");
        uploadFileService.updateById(file);

        UploadBatchEntity batch = uploadBatchService.getById(file.getBatchId());
        if (batch != null && batch.getDeleted() != null && batch.getDeleted() == 0) {
            int successCount = Math.max(0, safeInt(batch.getSuccessCount()) - 1);
            int failedCount = safeInt(batch.getFailedCount()) + 1;
            batch.setSuccessCount(successCount);
            batch.setFailedCount(failedCount);
            batch.setTotalFiles(successCount + failedCount);
            uploadBatchService.updateById(batch);
        }

        try {
            Files.deleteIfExists(Path.of(file.getFilePath()));
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "database record deleted, but local file deletion failed", e);
        }
    }

    private UploadBatchEntity createBatch(String sourceType, String batchName) {
        createDirectories(baseUploadPath());
        createDirectories(baseExtractPath());

        UploadBatchEntity batch = new UploadBatchEntity();
        batch.setBatchNo(generateBatchNo());
        batch.setBatchName(batchName);
        batch.setSourceType(sourceType);
        batch.setStatus("UPLOADED");
        batch.setTotalFiles(0);
        batch.setSuccessCount(0);
        batch.setFailedCount(0);
        batch.setDeleted(0);
        uploadBatchService.save(batch);
        return batch;
    }

    private void finishBatch(UploadBatchEntity batch, int successCount) {
        batch.setTotalFiles(successCount);
        batch.setSuccessCount(successCount);
        batch.setFailedCount(0);
        batch.setStatus("UPLOADED");
        uploadBatchService.updateById(batch);
    }

    private UploadFileEntity saveMultipartImage(Long batchId, MultipartFile file, Path basePath) {
        String originalFilename = cleanFilename(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        Path directory = dateDirectory(basePath);
        createDirectories(directory);
        String storedFileName = IdUtil.fastSimpleUUID() + "." + extension;
        Path targetPath = directory.resolve(storedFileName);
        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "failed to save image file", e);
        }
        return buildUploadFile(batchId, originalFilename, storedFileName, targetPath, extension);
    }

    private void saveOriginalZip(MultipartFile file, String batchNo) {
        Path directory = dateDirectory(baseUploadPath()).resolve(batchNo);
        createDirectories(directory);
        String storedFileName = IdUtil.fastSimpleUUID() + ".zip";
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, directory.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "failed to save original zip file", e);
        }
    }

    private UploadFileEntity buildUploadFile(Long batchId, String originalFilename, String storedFileName, Path targetPath, String extension) {
        UploadFileEntity uploadFile = new UploadFileEntity();
        uploadFile.setBatchId(batchId);
        uploadFile.setOriginalFilename(cleanFilename(originalFilename));
        uploadFile.setFileName(storedFileName);
        uploadFile.setFilePath(targetPath.toAbsolutePath().toString());
        uploadFile.setFileType(extension);
        try {
            uploadFile.setFileSize(Files.size(targetPath));
        } catch (IOException e) {
            uploadFile.setFileSize(0L);
        }
        fillImageSize(uploadFile, targetPath);
        uploadFile.setStatus("UPLOADED");
        uploadFile.setDeleted(0);
        return uploadFile;
    }

    private void fillImageSize(UploadFileEntity uploadFile, Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                uploadFile.setImageWidth(image.getWidth());
                uploadFile.setImageHeight(image.getHeight());
            }
        } catch (IOException ignored) {
            uploadFile.setImageWidth(null);
            uploadFile.setImageHeight(null);
        }
    }

    private UploadResponse buildUploadResponse(UploadBatchEntity batch, List<UploadFileEntity> fileEntities) {
        return UploadResponse.builder()
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .fileCount(fileEntities.size())
                .files(fileEntities.stream().map(this::toFileItem).toList())
                .build();
    }

    private UploadBatchListItemResponse toBatchListItem(UploadBatchEntity batch) {
        return UploadBatchListItemResponse.builder()
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .batchName(batch.getBatchName())
                .sourceType(batch.getSourceType())
                .status(batch.getStatus())
                .totalFiles(batch.getTotalFiles())
                .successCount(batch.getSuccessCount())
                .failedCount(batch.getFailedCount())
                .createdAt(batch.getCreatedAt())
                .build();
    }

    private UploadFileItemResponse toFileItem(UploadFileEntity file) {
        return UploadFileItemResponse.builder()
                .fileId(file.getId())
                .originalFilename(file.getOriginalFilename())
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .imageWidth(file.getImageWidth())
                .imageHeight(file.getImageHeight())
                .status(file.getStatus())
                .errorMessage(file.getErrorMessage())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private ImageFilePageItemResponse toImagePageItem(UploadFileEntity file) {
        UploadBatchEntity batch = uploadBatchService.getById(file.getBatchId());
        return ImageFilePageItemResponse.builder()
                .fileId(file.getId())
                .batchId(file.getBatchId())
                .batchNo(batch == null ? null : batch.getBatchNo())
                .sourceType(batch == null ? null : batch.getSourceType())
                .originalFilename(file.getOriginalFilename())
                .fileName(file.getFileName())
                .filePath(file.getFilePath())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .imageWidth(file.getImageWidth())
                .imageHeight(file.getImageHeight())
                .status(file.getStatus())
                .errorMessage(file.getErrorMessage())
                .previewUrl("/api/files/preview/" + file.getId())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private UploadFileEntity getActiveFile(Long fileId) {
        UploadFileEntity file = uploadFileService.getOne(
                new LambdaQueryWrapper<UploadFileEntity>()
                        .eq(UploadFileEntity::getId, fileId)
                        .eq(UploadFileEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (file == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "image record does not exist");
        }
        return file;
    }

    private MediaType mediaType(String fileType) {
        String type = normalizeFileType(fileType);
        if ("png".equals(type)) {
            return MediaType.IMAGE_PNG;
        }
        if ("jpg".equals(type) || "jpeg".equals(type)) {
            return MediaType.IMAGE_JPEG;
        }
        if ("webp".equals(type)) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String normalizeFileType(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT).trim();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void validateImageFile(String originalFilename) {
        String extension = getExtension(originalFilename);
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "only jpg, jpeg, png and webp images are allowed");
        }
    }

    private String getExtension(String filename) {
        String cleanName = cleanFilename(filename);
        int index = cleanName.lastIndexOf('.');
        if (index < 0 || index == cleanName.length() - 1) {
            return "";
        }
        return cleanName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String cleanFilename(String filename) {
        String cleanName = StringUtils.cleanPath(filename == null ? "unknown" : filename);
        return Path.of(cleanName).getFileName().toString();
    }

    private Path dateDirectory(Path basePath) {
        LocalDate today = LocalDate.now();
        return basePath.resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .resolve(String.format("%02d", today.getDayOfMonth()));
    }

    private Path baseUploadPath() {
        return Path.of(fileProperties.getUploadPath());
    }

    private Path baseExtractPath() {
        return Path.of(fileProperties.getExtractPath());
    }

    private void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "failed to create local file directory", e);
        }
    }

    private String generateBatchNo() {
        for (int i = 0; i < 5; i++) {
            int random = ThreadLocalRandom.current().nextInt(1000, 10000);
            String batchNo = "BATCH_" + LocalDateTime.now().format(BATCH_TIME_FORMATTER) + "_" + random;
            boolean exists = uploadBatchService.count(
                    new LambdaQueryWrapper<UploadBatchEntity>().eq(UploadBatchEntity::getBatchNo, batchNo)
            ) > 0;
            if (!exists) {
                return batchNo;
            }
        }
        return "BATCH_" + LocalDateTime.now().format(BATCH_TIME_FORMATTER) + "_" + IdUtil.fastSimpleUUID().substring(0, 8);
    }
}
