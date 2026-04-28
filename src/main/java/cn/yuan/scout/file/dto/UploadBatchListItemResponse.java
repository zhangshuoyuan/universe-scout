package cn.yuan.scout.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UploadBatchListItemResponse {

    private Long batchId;

    private String batchNo;

    private String batchName;

    private String sourceType;

    private String status;

    private Integer totalFiles;

    private Integer successCount;

    private Integer failedCount;

    private LocalDateTime createdAt;
}
