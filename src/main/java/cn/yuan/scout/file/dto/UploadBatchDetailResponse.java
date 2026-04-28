package cn.yuan.scout.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UploadBatchDetailResponse {

    private Long batchId;

    private String batchNo;

    private String batchName;

    private String sourceType;

    private String status;

    private Integer totalFiles;

    private Integer successCount;

    private Integer failedCount;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<UploadFileItemResponse> files;
}
