package ai.llm.responses;

import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Response;

import java.net.Proxy;
import java.util.Map;
import java.util.function.Function;

public final class OpenAiResponsesApiUtil {
    private static final Gson GSON = new Gson();

    private OpenAiResponsesApiUtil() {
    }

    public static LlmApiResponse createResponse(String apiKey,
                                                String apiUrl,
                                                Integer timeout,
                                                ResponseCreateRequest request,
                                                Function<Response, Integer> convertErrorFunc,
                                                Map<String, String> headers) {
        return createResponse(apiKey, apiUrl, timeout, (Object) request, convertErrorFunc, headers);
    }

    public static LlmApiResponse createResponse(String apiKey,
                                                String apiUrl,
                                                Integer timeout,
                                                Object request,
                                                Function<Response, Integer> convertErrorFunc,
                                                Map<String, String> headers) {
        return OpenAiApiUtil.completions(apiKey, apiUrl, timeout, serializeRequest(request),
                ResponsesChatCompletionConverter::convertResponse,
                convertErrorFunc,
                headers,
                null);
    }

    public static LlmApiResponse createResponse(String apiKey,
                                                String apiUrl,
                                                Integer timeout,
                                                String json,
                                                Function<Response, Integer> convertErrorFunc,
                                                Map<String, String> headers,
                                                Proxy proxy) {
        return OpenAiApiUtil.completions(apiKey, apiUrl, timeout, normalizeSchemaJson(json),
                ResponsesChatCompletionConverter::convertResponse,
                convertErrorFunc,
                headers,
                proxy);
    }

    public static LlmApiResponse streamResponse(String apiKey,
                                                String apiUrl,
                                                Integer timeout,
                                                ResponseCreateRequest request,
                                                Function<Response, Integer> convertErrorFunc,
                                                Map<String, String> headers) {
        return streamResponse(apiKey, apiUrl, timeout, (Object) request, convertErrorFunc, headers);
    }

    public static LlmApiResponse streamResponse(String apiKey,
                                                String apiUrl,
                                                Integer timeout,
                                                Object request,
                                                Function<Response, Integer> convertErrorFunc,
                                                Map<String, String> headers) {
        return OpenAiApiUtil.streamCompletions(apiKey, apiUrl, timeout, serializeRequest(request),
                ResponsesChatCompletionConverter::convertStreamEvent,
                convertErrorFunc,
                headers,
                null);
    }

    public static LlmApiResponse streamResponse(String apiKey,
                                                String apiUrl,
                                                Integer timeout,
                                                String json,
                                                Function<Response, Integer> convertErrorFunc,
                                                Map<String, String> headers,
                                                Proxy proxy) {
        return OpenAiApiUtil.streamCompletions(apiKey, apiUrl, timeout, normalizeSchemaJson(json),
                ResponsesChatCompletionConverter::convertStreamEvent,
                convertErrorFunc,
                headers,
                proxy);
    }

    static String serializeRequest(Object request) {
        return normalizeSchemaJson(GSON.toJson(request));
    }

    private static String normalizeSchemaJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        normalizeObjectSchemas(root);
        return GSON.toJson(root);
    }

    private static void normalizeObjectSchemas(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                normalizeObjectSchemas(item);
            }
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement type = object.get("type");
        if ((type != null && type.isJsonPrimitive() && "object".equals(type.getAsString()))
                || object.has("properties")) {
            object.addProperty("additionalProperties", false);
        }
        for (java.util.Map.Entry<String, JsonElement> entry : object.entrySet()) {
            normalizeObjectSchemas(entry.getValue());
        }
    }
}
