package cn.yuan.scout.ai.client;

import cn.yuan.scout.ai.config.VisionModelProperties;
import cn.yuan.scout.ai.dto.VisionModelRequest;
import cn.yuan.scout.ai.dto.VisionModelResponse;
import cn.yuan.scout.common.exception.BizException;
import cn.yuan.scout.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "vision.model",
        name = "provider",
        havingValue = "openai-compatible",
        matchIfMissing = true
)
public class OpenAiCompatibleVisionModelClient implements VisionModelClient {

    private final VisionModelProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public VisionModelResponse parseImage(VisionModelRequest request) {
        validateConfig();
        try {
            String imageDataUrl = toImageDataUrl(request.getImagePath());
            Map<String, Object> payload = Map.of(
                    "model", properties.getModelName(),
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of("type", "text", "text", request.getPrompt()),
                                            Map.of(
                                                    "type", "image_url",
                                                    "image_url", Map.of("url", imageDataUrl)
                                            )
                                    )
                            )
                    )
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(buildChatCompletionsUrl()))
                    .timeout(Duration.ofSeconds(timeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds()))
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(ErrorCode.SYSTEM_ERROR, "视觉模型调用失败：" + response.body());
            }

            return VisionModelResponse.builder()
                    .modelName(properties.getModelName())
                    .responseRaw(extractMessageContent(response.body()))
                    .build();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "视觉模型调用异常：" + e.getMessage());
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "vision.model.base-url 不能为空");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "vision.model.api-key 不能为空");
        }
        if (!StringUtils.hasText(properties.getModelName())) {
            throw new BizException(ErrorCode.PARAM_ERROR, "vision.model.model-name 不能为空");
        }
    }

    private String buildChatCompletionsUrl() {
        String baseUrl = properties.getBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/chat/completions")) {
            return baseUrl;
        }
        return baseUrl + "/chat/completions";
    }

    private String toImageDataUrl(String imagePath) throws Exception {
        if (!StringUtils.hasText(imagePath)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "图片文件路径为空");
        }
        Path path = Path.of(imagePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "图片文件不存在：" + imagePath);
        }
        if (!Files.isReadable(path)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "图片文件不可读：" + imagePath);
        }

        String mimeType = detectMimeType(path);
        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        return "data:" + mimeType + ";base64," + base64;
    }

    private String detectMimeType(Path path) throws Exception {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (filename.endsWith(".png")) {
            return "image/png";
        }
        if (filename.endsWith(".webp")) {
            return "image/webp";
        }
        String probed = Files.probeContentType(path);
        return StringUtils.hasText(probed) ? probed : "application/octet-stream";
    }

    private String extractMessageContent(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isMissingNode() && !content.isNull()) {
            return content.asText();
        }
        throw new BizException(ErrorCode.SYSTEM_ERROR, "视觉模型响应中没有 choices[0].message.content");
    }

    private int timeoutSeconds() {
        Integer timeoutSeconds = properties.getTimeoutSeconds();
        return timeoutSeconds == null || timeoutSeconds <= 0 ? 60 : timeoutSeconds;
    }
}
