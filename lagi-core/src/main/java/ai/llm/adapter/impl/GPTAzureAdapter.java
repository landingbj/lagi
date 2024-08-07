package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
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

@LLM(modelNames = {"gpt-3.5-turbo","gpt-4-1106-preview","gpt-4o-20240513"})
public class GPTAzureAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GPTAzureAdapter.class);
    private final Gson gson = new Gson();
    private static final String ENTERPOINT = "https://api.openai.com/v1/chat/completions";
    private static final int HTTP_TIMEOUT = 30 * 1000;


    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    @Override
    public String getApiAddress() {
        return getEndpoint() + "openai/deployments/" + getDeployment() + "/chat/completions?api-version=" + getApiVersion();
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("api-key", getApiKey());
        String jsonResult = null;
        chatCompletionRequest.setCategory(null);
        try {
            jsonResult = HttpUtil.httpPost(getApiAddress(), headers, chatCompletionRequest, HTTP_TIMEOUT);
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
        String apiUrl = getApiAddress();
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
        Map<String,String> addHeader = new HashMap() {{
            put("api-key", apiKey);
        }};
        ObservableList<ChatCompletionResult> result =
                ServerSentEventUtil.streamCompletions(json, apiUrl, apiKey,addHeader, convertFunc, this);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
}
