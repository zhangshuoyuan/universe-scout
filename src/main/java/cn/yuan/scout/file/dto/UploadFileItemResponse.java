package cn.yuan.scout.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UploadFileItemResponse {

    private Long fileId;

    private String originalFilename;

    private String fileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Integer imageWidth;

    private Integer imageHeight;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;
}
