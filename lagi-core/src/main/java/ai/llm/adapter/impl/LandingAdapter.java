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

@LLM(modelNames = "qa")
public class LandingAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(LandingAdapter.class);
    private final Gson gson = new Gson();
    private static final int HTTP_TIMEOUT = 15 * 1000;
    private static final String API_ADDRESS = "http://ai.landingbj.com/v1/chat/completions";

    @Override
    public boolean verify() {
        if (getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return ai.utils.ApikeyUtil.isApiKeyValid(getApiKey());
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        String model = chatCompletionRequest.getModel();
        String url = API_ADDRESS;
        if (model.equals("cascade")) {
            chatCompletionRequest.setModel(null);
            chatCompletionRequest.setCategory(null);
            url = "https://lagi.saasai.top/v1/chat/completions";
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + getApiKey());
        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpPost(url, headers, chatCompletionRequest, HTTP_TIMEOUT);
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
        String url = API_ADDRESS;
        if (model.equals("cascade")) {
            chatCompletionRequest.setModel(null);
            chatCompletionRequest.setCategory(null);
            url = "https://lagi.saasai.top/v1/chat/completions";
        }
        String json = gson.toJson(chatCompletionRequest);
        String apiKey = getApiKey();
        Function<String, ChatCompletionResult> convertFunc = e -> {
            if (e.equals("[DONE]")) {
                return null;
            }
            ChatCompletionResult result = gson.fromJson(e, ChatCompletionResult.class);
            result.getChoices().forEach(choice -> {
                if (choice.getDelta() != null) {
                    choice.setMessage(choice.getDelta());
                    choice.setDelta(null);
                }
            });
            return result;
        };
        ObservableList<ChatCompletionResult> result = ServerSentEventUtil.streamCompletions(json, url, apiKey, convertFunc, this);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }
}
