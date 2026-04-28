package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PromptTemplateResponse {

    private Long id;

    private String templateCode;

    private String templateName;

    private String prompt;

    private String jsonSchema;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
