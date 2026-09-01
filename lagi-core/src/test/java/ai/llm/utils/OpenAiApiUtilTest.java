package ai.llm.utils;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiApiUtilTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void serializesMultimodalContentAsAnOpenAiContentArray() throws Exception {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("qwen3.7-plus");
        request.setMessages(Collections.singletonList(ChatMessage.builder()
                .role("user")
                .content("[{\"type\":\"text\",\"text\":\"图片上的文字是什么？\"},"
                        + "{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:image/png;base64,AAAA\","
                        + "\"detail\":\"high\"}}]")
                .build()));

        JsonNode payload = MAPPER.readTree(OpenAiApiUtil.serializeChatCompletionRequest(request));
        JsonNode content = payload.path("messages").get(0).path("content");

        assertTrue(content.isArray());
        assertEquals("图片上的文字是什么？", content.get(0).path("text").asText());
        assertEquals("image_url", content.get(1).path("type").asText());
        assertEquals("data:image/png;base64,AAAA", content.get(1).path("image_url").path("url").asText());
        assertEquals("high", content.get(1).path("image_url").path("detail").asText());
    }

    @Test
    void keepsOrdinaryTextAsAString() throws Exception {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMessages(Collections.singletonList(ChatMessage.builder()
                .role("user")
                .content("[\"this is ordinary text, not multimodal content\"]")
                .build()));

        JsonNode payload = MAPPER.readTree(OpenAiApiUtil.serializeChatCompletionRequest(request));
        JsonNode content = payload.path("messages").get(0).path("content");

        assertTrue(content.isTextual());
        assertFalse(content.isArray());
        assertEquals("[\"this is ordinary text, not multimodal content\"]", content.asText());
    }

    @Test
    void keepsImageContentStructuredWhenAnotherMessageIsPlainText() throws Exception {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMessages(Arrays.asList(
                ChatMessage.builder().role("user")
                        .content("[{\"type\":\"image_url\",\"image_url\":{\"url\":\"https://example.com/a.png\"}}]")
                        .build(),
                ChatMessage.builder().role("assistant").content("I will inspect the image.").build()));

        JsonNode payload = MAPPER.readTree(OpenAiApiUtil.serializeChatCompletionRequest(request));

        assertTrue(payload.path("messages").get(0).path("content").isArray());
        assertTrue(payload.path("messages").get(1).path("content").isTextual());
    }
}
