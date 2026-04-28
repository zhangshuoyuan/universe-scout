package cn.yuan.scout.file.service;

import cn.yuan.scout.common.result.PageResult;
import cn.yuan.scout.file.dto.ImageFilePageItemResponse;
import cn.yuan.scout.file.dto.UploadBatchDetailResponse;
import cn.yuan.scout.file.dto.UploadBatchListItemResponse;
import cn.yuan.scout.file.dto.UploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    UploadResponse uploadImages(MultipartFile[] files);

    UploadResponse uploadZip(MultipartFile file);

    PageResult<UploadBatchListItemResponse> pageBatches(Long pageNum, Long pageSize, String sourceType, String status);

    UploadBatchDetailResponse getBatchDetail(Long batchId);

    PageResult<ImageFilePageItemResponse> pageImages(Long pageNum, Long pageSize, Long batchId, String fileType, String keyword);

    ResponseEntity<Resource> previewImage(Long fileId);

    void deleteImage(Long fileId);
}
