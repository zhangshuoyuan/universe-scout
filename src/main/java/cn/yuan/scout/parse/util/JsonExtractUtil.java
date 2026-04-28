package cn.yuan.scout.parse.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonExtractUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonExtractUtil() {
    }

    public static String extractJson(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("模型返回为空");
        }

        String candidate = text.trim();
        if (candidate.startsWith("```")) {
            candidate = candidate.replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        if (!isJson(candidate)) {
            int start = candidate.indexOf('{');
            int end = candidate.lastIndexOf('}');
            if (start >= 0 && end > start) {
                candidate = candidate.substring(start, end + 1).trim();
            }
        }

        if (!isJson(candidate)) {
            throw new IllegalArgumentException("模型返回不是合法 JSON");
        }
        return candidate;
    }

    private static boolean isJson(String text) {
        try {
            JsonNode ignored = OBJECT_MAPPER.readTree(text);
            return ignored != null && ignored.isObject();
        } catch (Exception e) {
            return false;
        }
    }
}
