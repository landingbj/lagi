package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.utils.ObservableList;
import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import io.reactivex.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@LLM(modelNames = { })
public class WiseModelAdapter extends ModelService implements ILlmAdapter {

    private final String wiseApiAddress = "https://wisemodel.cn/apiserving/";

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest request) {
        Map<String, String> headers = buildHeaders();
        Gson  gson = new Gson();
        String json = gson.toJson(request);
        String post = ApiInvokeUtil.post(wiseApiAddress, headers, json, 60, TimeUnit.SECONDS);
        return gson.fromJson(post, ChatCompletionResult.class);
    }

    private  Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("publisher-name", getPublisher());
        headers.put("serving-name-en", getServerName());
        headers.put("api-key", getApiKey());
        return headers;
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        Map<String, String> headers = buildHeaders();
        Gson  gson = new Gson();
        String json = gson.toJson(chatCompletionRequest);
        ObservableList<ChatCompletionResult> sse = ApiInvokeUtil.sse(wiseApiAddress, headers, json, 60, TimeUnit.SECONDS, (a) -> {
            ChatCompletionResult chatCompletionResult = gson.fromJson(a, ChatCompletionResult.class);
            chatCompletionResult.getChoices().forEach(c->{
                ChatMessage message = c.getMessage();
                ChatMessage delta = c.getDelta();
                if(message == null && delta != null) {
                    c.setMessage(delta);
                    c.setDelta(null);
                }
            });
            return chatCompletionResult;
        });
        return sse.getObservable();
    }

    @Override
    public boolean verify() {
        if(getApiKey() == null || (!getApiKey().startsWith("wisemodel-"))) {
            return false;
        }
        return true;
    }

}
