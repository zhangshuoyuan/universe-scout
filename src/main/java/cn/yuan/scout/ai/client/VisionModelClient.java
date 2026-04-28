package cn.yuan.scout.ai.client;

import cn.yuan.scout.ai.dto.VisionModelRequest;
import cn.yuan.scout.ai.dto.VisionModelResponse;

public interface VisionModelClient {

    VisionModelResponse parseImage(VisionModelRequest request);
}
