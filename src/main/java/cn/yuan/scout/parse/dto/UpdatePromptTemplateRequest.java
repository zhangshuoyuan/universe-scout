package cn.yuan.scout.parse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePromptTemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "提示词不能为空")
    private String prompt;

    private String jsonSchema;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
