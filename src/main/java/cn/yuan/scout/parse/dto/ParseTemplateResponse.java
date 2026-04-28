package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParseTemplateResponse {

    private Long id;

    private String templateCode;

    private String templateName;

    private Integer status;

    private LocalDateTime createdAt;
}
