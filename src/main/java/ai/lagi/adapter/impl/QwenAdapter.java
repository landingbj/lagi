package ai.lagi.adapter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import ai.lagi.adapter.ILlmAdapter;
import ai.lagi.pojo.QwenCompletionRequest;
import ai.lagi.pojo.QwenInput;
import ai.lagi.pojo.QwenMessage;
import ai.lagi.pojo.QwenParameters;
import ai.lagi.pojo.QwenResponse;
import ai.migrate.pojo.Backend;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import ai.utils.qa.HttpUtil;

public class QwenAdapter implements ILlmAdapter {
    private static Gson gson = new Gson();
    private static final String COMPLETIONS_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final int HTTP_TIMEOUT = -15 * 1000;

    private Backend backendConfig;

    public QwenAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + backendConfig.getApi_key());
        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpPost(COMPLETIONS_URL, headers, convertRequest(request), HTTP_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonResult == null) {
            return null;
        }
        QwenResponse response = gson.fromJson(jsonResult, QwenResponse.class);
        if (response == null || response.getOutput() == null) {
            return null;
        }
        return convertResponse(response);
    }

    private QwenCompletionRequest convertRequest(ChatCompletionRequest request) {
        QwenCompletionRequest result = new QwenCompletionRequest();
        result.setModel(request.getModel());
        List<QwenMessage> messages = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            QwenMessage qwenMessage = new QwenMessage();
            qwenMessage.setRole(chatMessage.getRole());
            qwenMessage.setContent(chatMessage.getContent());
            messages.add(qwenMessage);
        }
        QwenInput qwenInput = new QwenInput();
        qwenInput.setMessages(messages);
        result.setInput(qwenInput);
        QwenParameters qwenParameters = new QwenParameters();
        qwenParameters.setMax_tokens(request.getMax_tokens());
        qwenParameters.setTemperature(request.getTemperature());
        result.setParameters(qwenParameters);
        return result;
    }

    private ChatCompletionResult convertResponse(QwenResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(response.getRequest_id());
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        result.setModel(this.backendConfig.getModel());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getOutput().getText());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason(response.getOutput().getFinish_reason());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        return result;
    }
}
