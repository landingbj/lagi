package ai.lagi.adapter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ai.lagi.utils.ObservableList;
import ai.lagi.utils.ServerSentEventUtil;
import com.google.gson.Gson;

import ai.lagi.adapter.ILlmAdapter;
import ai.migrate.pojo.Backend;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.qa.HttpUtil;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VicunaAdapter implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VicunaAdapter.class);
    private final Gson gson = new Gson();
    private static final int HTTP_TIMEOUT = 15 * 1000;
    private final Backend backendConfig;
    public VicunaAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + backendConfig.getApi_key());
        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpPost(backendConfig.getApi_address(), headers, request, HTTP_TIMEOUT);
        } catch (IOException e) {
            logger.error("", e);
        }
        if (jsonResult == null) {
            return null;
        }
        ChatCompletionResult response = gson.fromJson(jsonResult, ChatCompletionResult.class);
        if (response == null || response.getChoices().isEmpty()) {
            return null;
        }
        return response;
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        String apiUrl = backendConfig.getApi_address();
        String json = gson.toJson(chatCompletionRequest);
        String apiKey = backendConfig.getApi_key();
        Function<String, ChatCompletionResult> convertFunc = e -> {
            if (e.equals("[DONE]")) {
                return null;
            }
            return gson.fromJson(e, ChatCompletionResult.class);
        };
        ObservableList<ChatCompletionResult> result =
                ServerSentEventUtil.streamCompletions(json, apiUrl, apiKey, convertFunc);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }
}
