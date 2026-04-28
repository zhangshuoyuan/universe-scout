package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ParseTaskDetailResponse {

    private Long taskId;

    private String taskNo;

    private String templateCode;

    private String templateName;

    private String status;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long batchId;

    private String batchNo;

    private String sourceType;

    private Integer totalFiles;

    private List<ParseTaskFileItemResponse> files;

    private List<VisionParseResultResponse> results;
}
