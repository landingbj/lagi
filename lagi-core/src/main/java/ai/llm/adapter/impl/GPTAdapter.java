package ai.llm.adapter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ai.common.utils.ObservableList;
import ai.llm.utils.ServerSentEventUtil;
import com.google.gson.Gson;

import ai.llm.adapter.ILlmAdapter;
import ai.common.pojo.Backend;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.qa.HttpUtil;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPTAdapter implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GPTAdapter.class);
    private final Gson gson = new Gson();
    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final int HTTP_TIMEOUT = 5 * 1000;

    private final Backend backendConfig;

    public GPTAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + backendConfig.getApiKey());
        String jsonResult = null;
        chatCompletionRequest.setCategory(null);
        try {
            jsonResult = HttpUtil.httpPost(COMPLETIONS_URL, headers, chatCompletionRequest, HTTP_TIMEOUT);
            System.out.println(jsonResult);
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
        String json = gson.toJson(chatCompletionRequest);
        String apiKey = backendConfig.getApiKey();
        Function<String, ChatCompletionResult> convertFunc = e -> {
            if (e.equals("[DONE]")) {
                return null;
            }
            ChatCompletionResult result = gson.fromJson(e, ChatCompletionResult.class);
            result.getChoices().forEach(choice -> {
                choice.setMessage(choice.getDelta());
                choice.setDelta(null);
            });
            return result;
        };
        ObservableList<ChatCompletionResult> result =
                ServerSentEventUtil.streamCompletions(json, COMPLETIONS_URL, apiKey, convertFunc);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(backendConfig.getModel());
        }
    }
}
