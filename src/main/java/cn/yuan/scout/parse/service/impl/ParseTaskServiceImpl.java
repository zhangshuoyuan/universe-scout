package cn.yuan.scout.parse.service.impl;

import cn.yuan.scout.ai.client.VisionModelClient;
import cn.yuan.scout.ai.dto.VisionModelRequest;
import cn.yuan.scout.ai.dto.VisionModelResponse;
import cn.hutool.core.util.IdUtil;
import cn.yuan.scout.common.exception.BizException;
import cn.yuan.scout.common.exception.ErrorCode;
import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.file.entity.UploadBatchEntity;
import cn.yuan.scout.file.entity.UploadFileEntity;
import cn.yuan.scout.file.service.UploadBatchService;
import cn.yuan.scout.file.service.UploadFileService;
import cn.yuan.scout.parse.dto.CreateParseTaskRequest;
import cn.yuan.scout.parse.dto.CreateParseTaskResponse;
import cn.yuan.scout.parse.dto.ParseTaskDetailResponse;
import cn.yuan.scout.parse.dto.ParseTaskFileItemResponse;
import cn.yuan.scout.parse.dto.ParseTaskListItemResponse;
import cn.yuan.scout.parse.dto.ParseTemplateResponse;
import cn.yuan.scout.parse.dto.VisionParseResultResponse;
import cn.yuan.scout.parse.entity.ParseTaskEntity;
import cn.yuan.scout.parse.entity.ParseTemplateEntity;
import cn.yuan.scout.parse.entity.VisionParseResultEntity;
import cn.yuan.scout.parse.service.ParseTaskRecordService;
import cn.yuan.scout.parse.service.ParseTaskService;
import cn.yuan.scout.parse.service.ParseTemplateService;
import cn.yuan.scout.parse.service.VisionParseResultService;
import cn.yuan.scout.parse.util.JsonExtractUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ParseTaskServiceImpl implements ParseTaskService {

    private static final DateTimeFormatter TASK_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ParseTemplateService parseTemplateService;
    private final ParseTaskRecordService parseTaskRecordService;
    private final VisionParseResultService visionParseResultService;
    private final UploadBatchService uploadBatchService;
    private final UploadFileService uploadFileService;
    private final VisionModelClient visionModelClient;

    @Override
    public List<ParseTemplateResponse> listEnabledTemplates() {
        return parseTemplateService.list(
                        new LambdaQueryWrapper<ParseTemplateEntity>()
                                .eq(ParseTemplateEntity::getDeleted, 0)
                                .eq(ParseTemplateEntity::getStatus, 1)
                                .orderByAsc(ParseTemplateEntity::getCreatedAt)
                )
                .stream()
                .map(this::toTemplateResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateParseTaskResponse createTask(CreateParseTaskRequest request) {
        UploadBatchEntity batch = uploadBatchService.getOne(
                new LambdaQueryWrapper<UploadBatchEntity>()
                        .eq(UploadBatchEntity::getId, request.getBatchId())
                        .eq(UploadBatchEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (batch == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "上传批次不存在");
        }
        if (!"UPLOADED".equals(batch.getStatus())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "上传批次状态异常，不能创建解析任务");
        }

        long fileCount = uploadFileService.count(
                new LambdaQueryWrapper<UploadFileEntity>()
                        .eq(UploadFileEntity::getBatchId, request.getBatchId())
                        .eq(UploadFileEntity::getDeleted, 0)
                        .eq(UploadFileEntity::getStatus, "UPLOADED")
        );
        if (fileCount <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "上传批次下没有可解析文件");
        }

        ParseTemplateEntity template = getTemplate(request.getTemplateCode());
        if (template == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "解析模板不存在");
        }
        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new BizException(ErrorCode.PARAM_ERROR, "解析模板未启用");
        }

        ParseTaskEntity task = new ParseTaskEntity();
        task.setTaskNo(generateTaskNo());
        task.setBatchId(request.getBatchId());
        task.setTemplateCode(template.getTemplateCode());
        task.setStatus("PENDING");
        task.setTotalCount(Math.toIntExact(fileCount));
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setDeleted(0);
        parseTaskRecordService.save(task);

        return CreateParseTaskResponse.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .status(task.getStatus())
                .build();
    }

    @Override
    public PageResult<ParseTaskListItemResponse> pageTasks(Long pageNum, Long pageSize, String templateCode, String status) {
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<ParseTaskEntity> wrapper = new LambdaQueryWrapper<ParseTaskEntity>()
                .eq(ParseTaskEntity::getDeleted, 0)
                .eq(StringUtils.hasText(templateCode), ParseTaskEntity::getTemplateCode, templateCode)
                .eq(StringUtils.hasText(status), ParseTaskEntity::getStatus, status)
                .orderByDesc(ParseTaskEntity::getCreatedAt);

        Page<ParseTaskEntity> page = parseTaskRecordService.page(new Page<>(safePageNum, safePageSize), wrapper);
        List<ParseTaskListItemResponse> records = page.getRecords()
                .stream()
                .map(this::toTaskListItem)
                .toList();
        return PageResult.of(records, page.getTotal(), safePageNum, safePageSize);
    }

    @Override
    public ParseTaskDetailResponse getTaskDetail(Long taskId) {
        ParseTaskEntity task = parseTaskRecordService.getOne(
                new LambdaQueryWrapper<ParseTaskEntity>()
                        .eq(ParseTaskEntity::getId, taskId)
                        .eq(ParseTaskEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (task == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "解析任务不存在");
        }

        UploadBatchEntity batch = uploadBatchService.getById(task.getBatchId());
        ParseTemplateEntity template = getTemplate(task.getTemplateCode());
        List<ParseTaskFileItemResponse> files = uploadFileService.list(
                        new LambdaQueryWrapper<UploadFileEntity>()
                                .eq(UploadFileEntity::getBatchId, task.getBatchId())
                                .eq(UploadFileEntity::getDeleted, 0)
                                .eq(UploadFileEntity::getStatus, "UPLOADED")
                                .orderByAsc(UploadFileEntity::getCreatedAt)
                )
                .stream()
                .map(this::toFileItem)
                .toList();
        List<VisionParseResultResponse> results = visionParseResultService.list(
                        new LambdaQueryWrapper<VisionParseResultEntity>()
                                .eq(VisionParseResultEntity::getTaskId, taskId)
                                .eq(VisionParseResultEntity::getDeleted, 0)
                                .orderByAsc(VisionParseResultEntity::getCreatedAt)
                )
                .stream()
                .map(this::toResultResponse)
                .toList();

        return ParseTaskDetailResponse.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .templateCode(task.getTemplateCode())
                .templateName(template == null ? null : template.getTemplateName())
                .status(task.getStatus())
                .totalCount(task.getTotalCount())
                .successCount(task.getSuccessCount())
                .failedCount(task.getFailedCount())
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .batchId(task.getBatchId())
                .batchNo(batch == null ? null : batch.getBatchNo())
                .sourceType(batch == null ? null : batch.getSourceType())
                .totalFiles(batch == null ? null : batch.getTotalFiles())
                .files(files)
                .results(results)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTask(Long taskId) {
        ParseTaskEntity task = parseTaskRecordService.getOne(
                new LambdaQueryWrapper<ParseTaskEntity>()
                        .eq(ParseTaskEntity::getId, taskId)
                        .eq(ParseTaskEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (task == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "解析任务不存在");
        }
        if (!"PENDING".equals(task.getStatus()) && !"FAILED".equals(task.getStatus())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "任务状态不允许启动");
        }

        UploadBatchEntity batch = uploadBatchService.getById(task.getBatchId());
        if (batch == null || batch.getDeleted() == null || batch.getDeleted() != 0) {
            throw new BizException(ErrorCode.PARAM_ERROR, "上传批次不存在");
        }

        ParseTemplateEntity template = getTemplate(task.getTemplateCode());
        if (template == null || template.getStatus() == null || template.getStatus() != 1) {
            throw new BizException(ErrorCode.PARAM_ERROR, "解析模板不存在或未启用");
        }

        List<UploadFileEntity> files = uploadFileService.list(
                new LambdaQueryWrapper<UploadFileEntity>()
                        .eq(UploadFileEntity::getBatchId, task.getBatchId())
                        .eq(UploadFileEntity::getDeleted, 0)
                        .eq(UploadFileEntity::getStatus, "UPLOADED")
                        .orderByAsc(UploadFileEntity::getCreatedAt)
        );
        if (files.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "上传批次下没有可解析文件");
        }

        if ("FAILED".equals(task.getStatus())) {
            visionParseResultService.remove(
                    new LambdaQueryWrapper<VisionParseResultEntity>()
                            .eq(VisionParseResultEntity::getTaskId, taskId)
            );
        }

        task.setStatus("RUNNING");
        task.setTotalCount(files.size());
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setErrorMessage(null);
        parseTaskRecordService.updateById(task);

        int successCount = 0;
        int failedCount = 0;
        for (UploadFileEntity file : files) {
            VisionParseResultEntity result = parseOneImage(task, template, file);
            visionParseResultService.save(result);
            if ("SUCCESS".equals(result.getStatus())) {
                successCount++;
            } else {
                failedCount++;
            }
        }

        task.setSuccessCount(successCount);
        task.setFailedCount(failedCount);
        task.setStatus(finalStatus(successCount, failedCount, files.size()));
        task.setErrorMessage(failedCount == 0 ? null : "部分图片解析失败，请查看解析结果");
        parseTaskRecordService.updateById(task);
    }

    @Override
    public ResponseEntity<Resource> exportFailedImages(Long taskId) {
        ParseTaskEntity task = getTask(taskId);
        List<VisionParseResultEntity> failedResults = visionParseResultService.list(
                new LambdaQueryWrapper<VisionParseResultEntity>()
                        .eq(VisionParseResultEntity::getTaskId, taskId)
                        .eq(VisionParseResultEntity::getDeleted, 0)
                        .eq(VisionParseResultEntity::getStatus, "FAILED")
                        .orderByAsc(VisionParseResultEntity::getCreatedAt)
        );
        if (failedResults.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "no failed images to export");
        }

        byte[] zipBytes = buildFailedImagesZip(failedResults);
        String filename = task.getTaskNo() + "_failed_images.zip";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        ByteArrayResource resource = new ByteArrayResource(zipBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(zipBytes.length)
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }

    private VisionParseResultEntity parseOneImage(ParseTaskEntity task, ParseTemplateEntity template, UploadFileEntity file) {
        VisionParseResultEntity result = new VisionParseResultEntity();
        result.setTaskId(task.getId());
        result.setFileId(file.getId());
        result.setTemplateCode(task.getTemplateCode());
        result.setRequestPrompt(template.getPrompt());
        result.setDeleted(0);

        try {
            if (file.getFilePath() == null || file.getFilePath().isBlank()) {
                throw new IllegalArgumentException("图片文件路径为空");
            }
            VisionModelResponse response = visionModelClient.parseImage(
                    VisionModelRequest.builder()
                            .imagePath(file.getFilePath())
                            .prompt(template.getPrompt())
                            .templateCode(template.getTemplateCode())
                            .build()
            );
            String responseRaw = response == null ? null : response.getResponseRaw();
            String responseJson = JsonExtractUtil.extractJson(responseRaw);
            result.setModelName(response == null ? null : response.getModelName());
            result.setResponseRaw(responseRaw);
            result.setResponseJson(responseJson);
            result.setStatus("SUCCESS");
        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }
        return result;
    }

    private byte[] buildFailedImagesZip(List<VisionParseResultEntity> failedResults) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            Set<String> usedNames = new HashSet<>();
            int index = 1;
            for (VisionParseResultEntity result : failedResults) {
                UploadFileEntity file = uploadFileService.getById(result.getFileId());
                if (file == null || !StringUtils.hasText(file.getFilePath())) {
                    continue;
                }
                Path path = Path.of(file.getFilePath()).normalize();
                if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
                    continue;
                }
                String entryName = uniqueZipEntryName(index++, file.getOriginalFilename(), file.getFileName(), usedNames);
                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                Files.copy(path, zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            byte[] bytes = outputStream.toByteArray();
            if (bytes.length == 0) {
                throw new BizException(ErrorCode.PARAM_ERROR, "failed image files do not exist on disk");
            }
            return bytes;
        } catch (IOException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "failed to export failed images", e);
        }
    }

    private String uniqueZipEntryName(int index, String originalFilename, String storedFilename, Set<String> usedNames) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename : storedFilename;
        if (!StringUtils.hasText(filename)) {
            filename = "failed-image-" + index;
        }
        filename = Path.of(filename).getFileName().toString();
        String candidate = String.format("%03d_%s", index, filename);
        while (usedNames.contains(candidate)) {
            candidate = String.format("%03d_%s_%s", index, IdUtil.fastSimpleUUID().substring(0, 6), filename);
        }
        usedNames.add(candidate);
        return candidate;
    }

    private ParseTaskEntity getTask(Long taskId) {
        ParseTaskEntity task = parseTaskRecordService.getOne(
                new LambdaQueryWrapper<ParseTaskEntity>()
                        .eq(ParseTaskEntity::getId, taskId)
                        .eq(ParseTaskEntity::getDeleted, 0)
                        .last("limit 1")
        );
        if (task == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "parse task does not exist");
        }
        return task;
    }

    private String finalStatus(int successCount, int failedCount, int totalCount) {
        if (successCount == totalCount) {
            return "SUCCESS";
        }
        if (successCount > 0 && failedCount > 0) {
            return "PARTIAL_SUCCESS";
        }
        return "FAILED";
    }

    private ParseTemplateEntity getTemplate(String templateCode) {
        return parseTemplateService.getOne(
                new LambdaQueryWrapper<ParseTemplateEntity>()
                        .eq(ParseTemplateEntity::getTemplateCode, templateCode)
                        .eq(ParseTemplateEntity::getDeleted, 0)
                        .last("limit 1")
        );
    }

    private ParseTemplateResponse toTemplateResponse(ParseTemplateEntity template) {
        return ParseTemplateResponse.builder()
                .id(template.getId())
                .templateCode(template.getTemplateCode())
                .templateName(template.getTemplateName())
                .status(template.getStatus())
                .createdAt(template.getCreatedAt())
                .build();
    }

    private ParseTaskListItemResponse toTaskListItem(ParseTaskEntity task) {
        UploadBatchEntity batch = uploadBatchService.getById(task.getBatchId());
        ParseTemplateEntity template = getTemplate(task.getTemplateCode());
        return ParseTaskListItemResponse.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .batchId(task.getBatchId())
                .batchNo(batch == null ? null : batch.getBatchNo())
                .templateCode(task.getTemplateCode())
                .templateName(template == null ? null : template.getTemplateName())
                .status(task.getStatus())
                .totalCount(task.getTotalCount())
                .successCount(task.getSuccessCount())
                .failedCount(task.getFailedCount())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private ParseTaskFileItemResponse toFileItem(UploadFileEntity file) {
        return ParseTaskFileItemResponse.builder()
                .fileId(file.getId())
                .originalFilename(file.getOriginalFilename())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .status(file.getStatus())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private VisionParseResultResponse toResultResponse(VisionParseResultEntity result) {
        UploadFileEntity file = uploadFileService.getById(result.getFileId());
        return VisionParseResultResponse.builder()
                .resultId(result.getId())
                .fileId(result.getFileId())
                .originalFilename(file == null ? null : file.getOriginalFilename())
                .modelName(result.getModelName())
                .responseRaw(result.getResponseRaw())
                .responseJson(result.getResponseJson())
                .status(result.getStatus())
                .errorMessage(result.getErrorMessage())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private String generateTaskNo() {
        for (int i = 0; i < 5; i++) {
            int random = ThreadLocalRandom.current().nextInt(1000, 10000);
            String taskNo = "TASK_" + LocalDateTime.now().format(TASK_TIME_FORMATTER) + "_" + random;
            boolean exists = parseTaskRecordService.count(
                    new LambdaQueryWrapper<ParseTaskEntity>().eq(ParseTaskEntity::getTaskNo, taskNo)
            ) > 0;
            if (!exists) {
                return taskNo;
            }
        }
        return "TASK_" + LocalDateTime.now().format(TASK_TIME_FORMATTER) + "_" + IdUtil.fastSimpleUUID().substring(0, 8);
    }
}
