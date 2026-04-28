package cn.yuan.scout.file.controller;

import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.common.result.Result;
import cn.yuan.scout.file.dto.ImageFilePageItemResponse;
import cn.yuan.scout.file.dto.UploadBatchDetailResponse;
import cn.yuan.scout.file.dto.UploadBatchListItemResponse;
import cn.yuan.scout.file.dto.UploadResponse;
import cn.yuan.scout.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/images")
    public Result<UploadResponse> uploadImages(@RequestPart("files") MultipartFile[] files) {
        return Result.success(fileStorageService.uploadImages(files));
    }

    @PostMapping("/upload/zip")
    public Result<UploadResponse> uploadZip(@RequestPart("file") MultipartFile file) {
        return Result.success(fileStorageService.uploadZip(file));
    }

    @GetMapping("/batches")
    public Result<PageResult<UploadBatchListItemResponse>> pageBatches(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String status
    ) {
        return Result.success(fileStorageService.pageBatches(pageNum, pageSize, sourceType, status));
    }

    @GetMapping("/batches/{batchId}")
    public Result<UploadBatchDetailResponse> getBatchDetail(@PathVariable Long batchId) {
        return Result.success(fileStorageService.getBatchDetail(batchId));
    }

    @GetMapping("/images")
    public Result<PageResult<ImageFilePageItemResponse>> pageImages(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "12") Long pageSize,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(fileStorageService.pageImages(pageNum, pageSize, batchId, fileType, keyword));
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> previewImage(@PathVariable Long fileId) {
        return fileStorageService.previewImage(fileId);
    }

    @DeleteMapping("/images/{fileId}")
    public Result<Void> deleteImage(@PathVariable Long fileId) {
        fileStorageService.deleteImage(fileId);
        return Result.success();
    }
}
