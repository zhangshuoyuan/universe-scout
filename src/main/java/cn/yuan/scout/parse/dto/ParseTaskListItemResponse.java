package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParseTaskListItemResponse {

    private Long taskId;

    private String taskNo;

    private Long batchId;

    private String batchNo;

    private String templateCode;

    private String templateName;

    private String status;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
