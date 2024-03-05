package ai.lagi.adapter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import ai.lagi.adapter.ILlmAdapter;
import ai.migrate.pojo.Backend;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.LLMConfig;
import ai.utils.qa.HttpUtil;

public class GPTAdapter implements ILlmAdapter {
    private static Gson gson = new Gson();
    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final int HTTP_TIMEOUT = -15 * 1000;
    
    private Backend backendConfig;

    public GPTAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + backendConfig.getApi_key());
        String jsonResult = null;
        request.setCategory(null);
        try {
            jsonResult = HttpUtil.httpPost(COMPLETIONS_URL, headers, request, HTTP_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonResult == null) {
            return null;
        }
        ChatCompletionResult response = gson.fromJson(jsonResult, ChatCompletionResult.class);
        if (response == null || response.getChoices().size() == 0) {
            return null;
        }
        return response;
    }

}
