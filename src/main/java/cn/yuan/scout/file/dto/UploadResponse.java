package cn.yuan.scout.file.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadResponse {

    private Long batchId;

    private String batchNo;

    private Integer fileCount;

    private List<UploadFileItemResponse> files;
}
