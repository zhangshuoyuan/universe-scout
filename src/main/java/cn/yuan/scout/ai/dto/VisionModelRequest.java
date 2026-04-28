package cn.yuan.scout.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VisionModelRequest {

    private String imagePath;

    private String prompt;

    private String templateCode;
}
