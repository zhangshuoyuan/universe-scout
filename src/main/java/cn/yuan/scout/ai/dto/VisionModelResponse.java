package cn.yuan.scout.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VisionModelResponse {

    private String modelName;

    private String responseRaw;
}
