package cn.yuan.scout.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vision.model")
public class VisionModelProperties {

    /**
     * mock or openai-compatible.
     */
    private String provider = "openai-compatible";

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer timeoutSeconds;
}
