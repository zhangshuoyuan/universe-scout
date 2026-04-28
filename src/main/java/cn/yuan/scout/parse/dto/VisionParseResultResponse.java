package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VisionParseResultResponse {

    private Long resultId;

    private Long fileId;

    private String originalFilename;

    private String modelName;

    private String responseRaw;

    private String responseJson;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;
}
