package cn.yuan.scout.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParseTaskFileItemResponse {

    private Long fileId;

    private String originalFilename;

    private String fileType;

    private Long fileSize;

    private String status;

    private LocalDateTime createdAt;
}
