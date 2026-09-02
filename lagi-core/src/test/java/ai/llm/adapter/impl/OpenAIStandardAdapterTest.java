package ai.llm.adapter.impl;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAIStandardAdapterTest {

    @Test
    void retriesOnceWhenFirstStreamContainsOnlyEmptyDeltas() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = createServer(requests, false);
        server.start();

        try {
            List<ChatCompletionResult> results = adapter(server).streamCompletions(streamRequest())
                    .timeout(5, TimeUnit.SECONDS)
                    .toList()
                    .blockingGet();

            assertEquals(2, requests.get());
            assertFalse(results.isEmpty());
            assertTrue(results.stream().noneMatch(result -> "empty-attempt".equals(result.getId())));
            assertTrue(results.stream().anyMatch(result -> result.getChoices() != null
                    && !result.getChoices().isEmpty()
                    && result.getChoices().get(0).getDelta() != null
                    && "图片中的模型支持图文理解。".equals(result.getChoices().get(0).getDelta().getContent())));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void returnsAnErrorChunkAfterSecondEmptyStream() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = createServer(requests, true);
        server.start();

        try {
            List<ChatCompletionResult> results = adapter(server).streamCompletions(streamRequest())
                    .timeout(5, TimeUnit.SECONDS)
                    .toList()
                    .blockingGet();

            assertEquals(2, requests.get());
            assertEquals(1, results.size());
            assertTrue(results.get(0).getChoices().get(0).getDelta().getContent()
                    .contains("The upstream model returned an empty response after one retry."));
            assertEquals("stop", results.get(0).getChoices().get(0).getFinish_reason());
        } finally {
            server.stop(0);
        }
    }

    private HttpServer createServer(AtomicInteger requests, boolean alwaysEmpty) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().close();
            int attempt = requests.incrementAndGet();
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            String body = !alwaysEmpty && attempt == 2 ? successfulStream() : emptyStream();
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
        });
        return server;
    }

    private OpenAIStandardAdapter adapter(HttpServer server) {
        OpenAIStandardAdapter adapter = new OpenAIStandardAdapter();
        adapter.setApiKey("test-key");
        adapter.setApiAddress("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions");
        return adapter;
    }

    private ChatCompletionRequest streamRequest() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel("vision-test");
        request.setStream(true);
        request.setMessages(Collections.singletonList(ChatMessage.builder()
                .role("user")
                .content("识别图片")
                .build()));
        return request;
    }

    private String emptyStream() {
        return "data: {\"id\":\"empty-attempt\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"\",\"reasoning_content\":\"\"},\"finish_reason\":null}]}\n\n"
                + "data: {\"id\":\"empty-attempt\",\"choices\":[],\"usage\":{\"prompt_tokens\":8,\"completion_tokens\":0,\"total_tokens\":8}}\n\n"
                + "data: [DONE]\n\n";
    }

    private String successfulStream() {
        return "data: {\"id\":\"successful-retry\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}\n\n"
                + "data: {\"id\":\"successful-retry\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"图片中的模型支持图文理解。\"},\"finish_reason\":null}]}\n\n"
                + "data: {\"id\":\"successful-retry\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n"
                + "data: [DONE]\n\n";
    }

}
