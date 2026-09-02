package ai.openai.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.function.Function;

/**
 * Utilities for the OpenAI-compatible multi-content message representation.
 * Internally, ChatMessage.content stores the JSON array as a String.
 */
public final class MultiModalContentUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MultiModalContentUtil() {
    }

    /**
     * Parses a supported OpenAI content array. Returns {@code null} for plain
     * text or for a structure that is not safe to treat as multimodal content.
     */
    public static ArrayNode parseOpenAiContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(content);
            if (!(node instanceof ArrayNode) || node.isEmpty()) {
                return null;
            }
            for (JsonNode part : node) {
                if (!(part instanceof ObjectNode)) {
                    return null;
                }
                String type = part.path("type").asText();
                if ("text".equals(type) && part.path("text").isTextual()) {
                    continue;
                }
                if ("image_url".equals(type)
                        && part.path("image_url").isObject()
                        && part.path("image_url").path("url").isTextual()) {
                    continue;
                }
                return null;
            }
            return (ArrayNode) node;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean hasImageContent(String content) {
        ArrayNode parts = parseOpenAiContent(content);
        if (parts == null) {
            return false;
        }
        for (JsonNode part : parts) {
            if ("image_url".equals(part.path("type").asText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies a text transformation only to text parts. Image URLs, including
     * data URLs and their Base64 payloads, are deliberately never inspected or
     * modified by this method.
     */
    public static String transformTextParts(String content, Function<String, String> transformer) {
        ArrayNode parts = parseOpenAiContent(content);
        if (parts == null || transformer == null) {
            return content;
        }

        boolean changed = false;
        for (JsonNode part : parts) {
            if (!"text".equals(part.path("type").asText())) {
                continue;
            }
            ObjectNode textPart = (ObjectNode) part;
            String original = textPart.path("text").asText();
            String transformed = transformer.apply(original);
            if (transformed == null) {
                transformed = "";
            }
            if (!original.equals(transformed)) {
                textPart.put("text", transformed);
                changed = true;
            }
        }
        if (!changed) {
            return content;
        }
        try {
            return MAPPER.writeValueAsString(parts);
        } catch (Exception ignored) {
            return content;
        }
    }
}
