package ai.llm.utils;

import ai.common.utils.ObservableList;
import ai.llm.pojo.LlmApiResponse;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.hutool.core.text.StrFormatter;
import com.google.gson.Gson;
import io.reactivex.Observable;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class OpenAiApiUtil {

    private static final Gson gson = new Gson();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(
            10, // 最大空闲连接数
            60, // 保持连接的时间
            TimeUnit.MINUTES
    );
    private static final Logger log = LoggerFactory.getLogger(OpenAiApiUtil.class);

    public static LlmApiResponse completions(String apikey, String apiUrl,
                                             Integer timeout,
                                             ChatCompletionRequest req,
                                             Function<String, ChatCompletionResult> convertResponseFunc,
                                             Function<Response, Integer> convertErrorFunc) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apikey);
        return completions(apikey, apiUrl, timeout, req, convertResponseFunc, convertErrorFunc, headers);
    }

    public static LlmApiResponse streamCompletions(String apikey, String apiUrl,
                                                   Integer timeout,
                                                   ChatCompletionRequest req,
                                                   Function<String, ChatCompletionResult> convertResponseFunc,
                                                   Function<Response, Integer> convertErrorFunc) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apikey);
        return streamCompletions(apikey, apiUrl, timeout, req, convertResponseFunc, convertErrorFunc, headers);
    }

    public static LlmApiResponse completions(String apikey, String apiUrl,
                                             Integer timeout,
                                             ChatCompletionRequest req,
                                             Function<String, ChatCompletionResult> convertResponseFunc,
                                             Function<Response, Integer> convertErrorFunc,
                                             Map<String, String> headers) {
        return completions(apikey, apiUrl, timeout, serializeChatCompletionRequest(req), convertResponseFunc,
                convertErrorFunc, headers, null);
    }

    public static LlmApiResponse completions(String apikey, String apiUrl,
                                             Integer timeout,
                                             String json,
                                             Function<String, ChatCompletionResult> convertResponseFunc,
                                             Function<Response, Integer> convertErrorFunc,
                                             Map<String, String> headers, Proxy proxy) {
        java.net.Authenticator.setDefault(new java.net.Authenticator() {
            @Override
            protected java.net.PasswordAuthentication getPasswordAuthentication() {
                return new java.net.PasswordAuthentication("socks5", "digimeta".toCharArray());
            }
        });
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .connectionPool(CONNECTION_POOL)
                .proxy(proxy)
                .build();
        MediaType mediaType = MediaType.get("application/json");
        RequestBody body = RequestBody.create(json, mediaType);
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiUrl)
                .post(body);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        Request request = requestBuilder.build();
        LlmApiResponse result = LlmApiResponse.builder().build();
        try (Response response = client.newCall(request).execute();) {
            String bodyStr = response.body().string();
            if (response.code() != 200) {
                Integer code = convertErrorFunc.apply(response);
                result.setCode(code);
                result.setMsg(bodyStr);
                return result;
            }
            result.setCode(200);
            result.setData(convertResponseFunc.apply(bodyStr));
        } catch (IOException e) {
            result.setCode(LLMErrorConstants.TIME_OUT);
            result.setMsg(e.getMessage());
            log.error(e.getMessage());
        }
        return result;
    }

    public static LlmApiResponse streamCompletions(String apikey, String apiUrl,
                                                   Integer timeout,
                                                   ChatCompletionRequest req,
                                                   Function<String, ChatCompletionResult> convertResponseFunc,
                                                   Function<Response, Integer> convertErrorFunc, Map<String, String> headers) {
        return streamCompletions(apikey, apiUrl, timeout, serializeChatCompletionRequest(req), convertResponseFunc,
                convertErrorFunc, headers, null);
    }

    /**
     * The public API stores multimodal message content as a JSON string so the
     * internal request type can serve both text and image inputs. Gson does not
     * honour {@code ContentSerializer}, so convert only recognised multimodal
     * content back to an array before forwarding an OpenAI-compatible request.
     */
    static String serializeChatCompletionRequest(ChatCompletionRequest request) {
        String payload = gson.toJson(request);
        try {
            JsonNode root = JSON_MAPPER.readTree(payload);
            JsonNode messages = root.get("messages");
            if (!messages.isArray()) {
                return payload;
            }
            for (JsonNode message : messages) {
                if (!(message instanceof ObjectNode)) {
                    continue;
                }
                ObjectNode objectMessage = (ObjectNode) message;
                JsonNode content = objectMessage.get("content");
                if (content == null || !content.isTextual()) {
                    continue;
                }
                try {
                    JsonNode multimodal = JSON_MAPPER.readTree(content.asText());
                    if (isMultimodalContent(multimodal)) {
                        objectMessage.set("content", multimodal);
                    }
                } catch (Exception ignored) {
                    // This message is ordinary text rather than multimodal JSON.
                }
            }
            return JSON_MAPPER.writeValueAsString(root);
        } catch (Exception ignored) {
            // Plain text may look like JSON. Keep the original Gson payload if
            // it is not a valid recognised multimodal message.
            return payload;
        }
    }

    private static boolean isMultimodalContent(JsonNode content) {
        if (!(content instanceof ArrayNode) || content.isEmpty()) {
            return false;
        }
        for (JsonNode part : content) {
            if (!part.isObject()) {
                return false;
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
            return false;
        }
        return true;
    }

    public static LlmApiResponse streamCompletions(String apikey, String apiUrl,
                                                   Integer timeout,
                                                   String json,
                                                   Function<String, ChatCompletionResult> convertResponseFunc,
                                                   Function<Response, Integer> convertErrorFunc, Map<String, String> headers) {
        return streamCompletions(apikey, apiUrl, timeout, json, convertResponseFunc, convertErrorFunc, headers, null);
    }
    public static LlmApiResponse streamCompletions(String apikey, String apiUrl,
                                                   Integer timeout,
                                                   String json,
                                                   Function<String, ChatCompletionResult> convertResponseFunc,
                                                   Function<Response, Integer> convertErrorFunc, Map<String, String> headers,
                                                   Proxy proxy) {
//        java.net.Authenticator.setDefault(new java.net.Authenticator() {
//            @Override
//            protected java.net.PasswordAuthentication getPasswordAuthentication() {
//                return new java.net.PasswordAuthentication("socks5", "digimeta".toCharArray());
//            }
//        });
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .connectionPool(CONNECTION_POOL)
//                .proxy(proxy)
//                .proxyAuthenticator(proxyAuthenticator)
                .build();
        MediaType mediaType = MediaType.get("application/json");
        RequestBody body = RequestBody.create(json, mediaType);
        Request.Builder requestBuilder = new Request.Builder()
                .url(apiUrl)
                .header("Accept", "text/event-stream")
                .header("timestamp", String.valueOf(System.currentTimeMillis()))
                .post(body);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        Request request = requestBuilder.build();
        LlmApiResponse result = LlmApiResponse.builder().build();
        EventSource.Factory factory = EventSources.createFactory(client);
        ObservableList<ChatCompletionResult> res = new ObservableList<>();
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
//                log.info("ServerSentEventUtil streamCompletions onOpen: response = {}", response);
                int code = response.code();
                try {
                    String bodyStr = response.body().string();
                    if(code != 200) {
                        result.setCode(convertErrorFunc.apply(response));
                        result.setMsg(bodyStr);
                        closeConnection(eventSource);
                    } else {
                        result.setCode(code);
                        result.setMsg(bodyStr);
                    }
                } catch (IOException e) {

                }
            }
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
//                log.info("ServerSentEventUtil streamCompletions onEvent: data = {}" , data);
                ChatCompletionResult chatCompletionResult = convertResponseFunc.apply(data);
                if (chatCompletionResult != null) {
                    res.add(chatCompletionResult);
                }
            }
            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                if(t instanceof SocketTimeoutException) {
                    result.setCode(LLMErrorConstants.TIME_OUT);
                    result.setMsg(StrFormatter.format("{\"error\":\"{}\"}", t.getMessage()));
                } else {
                    try {
                        String bodyStr = response.body().string();
                        result.setCode(convertErrorFunc.apply(response));
                        result.setMsg(bodyStr);
                    } catch (Exception e) {
                        result.setCode(LLMErrorConstants.OTHER_ERROR);
                        result.setMsg(StrFormatter.format("{\"error\":\"{}\"}", t.getMessage()));
                    }
                }
                if(t != null) {
                    log.error("model request failed error {}", t.getMessage());
                }
                closeConnection(eventSource);
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                closeConnection(eventSource);
            }

            private void closeConnection(EventSource eventSource) {
                res.onComplete();
                eventSource.cancel();
                client.dispatcher().executorService().shutdown();
            }
        });
        Iterable<ChatCompletionResult> iterable = res.getObservable().blockingIterable();
        iterable.iterator().hasNext();
        result.setStreamData(Observable.fromIterable(iterable));
//        result.setStreamData(res.getObservable());
        return result;
    }


}
