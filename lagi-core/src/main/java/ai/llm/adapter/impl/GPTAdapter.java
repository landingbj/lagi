package ai.llm.adapter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.utils.ObservableList;
import ai.llm.utils.ServerSentEventUtil;
import com.google.gson.Gson;

import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.qa.HttpUtil;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LLM(modelNames = {"gpt-3.5-turbo","gpt-4-1106-preview"})
public class GPTAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GPTAdapter.class);
    private final Gson gson = new Gson();
    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final int HTTP_TIMEOUT = 15 * 1000;



    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + getApiKey());
        String jsonResult = null;
        chatCompletionRequest.setCategory(null);
        try {
            jsonResult = HttpUtil.httpPost(COMPLETIONS_URL, headers, chatCompletionRequest, HTTP_TIMEOUT);
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
        chatCompletionRequest.setCategory(null);
        String json = gson.toJson(chatCompletionRequest);
        String apiKey = getApiKey();
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
                ServerSentEventUtil.streamCompletions(json, COMPLETIONS_URL, apiKey, convertFunc, this);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
}
