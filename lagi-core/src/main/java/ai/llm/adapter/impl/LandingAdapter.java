package ai.llm.adapter.impl;

import ai.common.pojo.Backend;
import ai.common.utils.ObservableList;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.utils.ServerSentEventUtil;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.qa.HttpUtil;
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LandingAdapter implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(LandingAdapter.class);
    private final Gson gson = new Gson();
    private static final int HTTP_TIMEOUT = 15 * 1000;
    private final Backend backendConfig;
    private static final String API_ADDRESS = "http://lagi.saasai.top:8090/api/v1/chat/completions";

    public LandingAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
        this.backendConfig.setApiAddress(API_ADDRESS);
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + backendConfig.getApiKey());
        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpPost(backendConfig.getApiAddress(), headers, chatCompletionRequest, HTTP_TIMEOUT);
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
        setDefaultModel(chatCompletionRequest);
        String apiUrl = backendConfig.getApiAddress();
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
        ObservableList<ChatCompletionResult> result = ServerSentEventUtil.streamCompletions(json, apiUrl, apiKey, convertFunc);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        request.setModel(null);
    }
}
