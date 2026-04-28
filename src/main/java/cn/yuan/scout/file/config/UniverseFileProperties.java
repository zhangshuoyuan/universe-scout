package cn.yuan.scout.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "universe.file")
public class UniverseFileProperties {

    private String uploadPath;

    private String extractPath;
}
