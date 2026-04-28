package cn.yuan.scout.parse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateParseTaskRequest {

    @NotNull(message = "上传批次不能为空")
    private Long batchId;

    @NotBlank(message = "解析模板不能为空")
    private String templateCode;
}
