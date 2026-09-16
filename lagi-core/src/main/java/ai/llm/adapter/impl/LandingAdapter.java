package ai.llm.adapter.impl;


import ai.common.db.HikariDS;
import ai.config.ConfigUtil;
import ai.llm.utils.ExtraBodyUtil;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.utils.MultimodalTraceUtil;
import ai.openai.pojo.ExtraBody;
import ai.utils.ApikeyUtil;
import io.reactivex.Observable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LandingAdapter extends OpenAIStandardAdapter {
    private final String DEFAULT_BASE_URL = "https://lagi.saasai.top";

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        if (isFinalServer()) {
            normalizeModelNameIfSlashSeparated(chatCompletionRequest);
            if (ApikeyUtil.isLandingKey(chatCompletionRequest.getApiKey())) {
                chatCompletionRequest.setApiKey(ApikeyUtil.getModelKey(chatCompletionRequest.getModel()));
            }
        }
        MultimodalTraceUtil.logRequest("landing-dispatch", chatCompletionRequest, getApiAddress());
        return super.completions(chatCompletionRequest);
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        if (isFinalServer()) {
            normalizeModelNameIfSlashSeparated(chatCompletionRequest);
            if (ApikeyUtil.isLandingKey(chatCompletionRequest.getApiKey())) {
                chatCompletionRequest.setApiKey(ApikeyUtil.getModelKey(chatCompletionRequest.getModel()));
            }
        }
        MultimodalTraceUtil.logRequest("landing-dispatch", chatCompletionRequest, getApiAddress());
        return super.streamCompletions(chatCompletionRequest);
    }

    /**
     * If model is like {@code Alibaba/qwen3-max}, keep only the part after the slash ({@code qwen3-max}).
     */
    private void normalizeModelNameIfSlashSeparated(ChatCompletionRequest chatCompletionRequest) {
        if (chatCompletionRequest == null || chatCompletionRequest.getModel() == null) {
            return;
        }
        String model = chatCompletionRequest.getModel().trim();
        int slash = model.indexOf('/');
        if (slash >= 0 && slash < model.length() - 1) {
            String after = model.substring(slash + 1).trim();
            if (!after.isEmpty()) {
                chatCompletionRequest.setModel(after);
            }
        }
    }

    private boolean isFinalServer() {
        String baseUrl = ConfigUtil.getBaseUrl();
        return baseUrl != null && !baseUrl.trim().isEmpty() && getApiAddress().startsWith(baseUrl);
    }

    @Override
    public String getApiAddress() {
        if (apiAddress == null) {
            apiAddress = DEFAULT_BASE_URL + "/v1/chat/completions";
        }
        return apiAddress;
    }

    @Override
    public void setApiAddress(String apiAddress) {
        this.apiAddress = apiAddress;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public void setDefaultField(ChatCompletionRequest request) {
        ExtraBody extraBody;
        if (request.getExtraBody() == null && ConfigUtil.isCascadeProxyEnabled()) {
            extraBody = ExtraBodyUtil.getExtraBody();
        } else {
            extraBody = request.getExtraBody();
        }
        String userApiKey = request.getUserApiKey();
        super.setDefaultField(request);
        request.setExtraBody(extraBody);
        request.setUserApiKey(userApiKey);
    }

}
