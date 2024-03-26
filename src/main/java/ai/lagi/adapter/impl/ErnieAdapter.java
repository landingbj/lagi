package ai.lagi.adapter.impl;

import ai.lagi.adapter.ILlmAdapter;
import ai.migrate.pojo.Backend;
import ai.openai.pojo.*;
import ai.utils.qa.ChatCompletionUtil;
import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.core.auth.Auth;
import com.baidubce.qianfan.model.chat.ChatRequest;
import com.baidubce.qianfan.model.chat.ChatResponse;
import com.baidubce.qianfan.model.chat.Message;
import com.baidubce.qianfan.model.constant.ModelEndpoint;

import java.util.ArrayList;
import java.util.List;

public class ErnieAdapter implements ILlmAdapter {
    private Backend backendConfig;
    private String endpoint;

    public ErnieAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
        endpoint = ModelEndpoint.getEndpoint(ModelEndpoint.CHAT, this.backendConfig.getModel());
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        String secretKey = this.backendConfig.getSecret_key();
        String apiKey = this.backendConfig.getApi_key();
        Qianfan qianfan = new Qianfan(Auth.TYPE_OAUTH, apiKey, secretKey);
        ChatRequest request = convertRequest(chatCompletionRequest);
        ChatResponse response = qianfan.chatCompletion(request);
        return convertResponse(response);
    }

    private ChatRequest convertRequest(ChatCompletionRequest request) {
        ChatRequest result = new ChatRequest();
        List<Message> messages = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            Message message = new Message();
            message.setRole(chatMessage.getRole());
            message.setContent(chatMessage.getContent());
            messages.add(message);
        }
        result.setMessages(messages);
        result.setTemperature(request.getTemperature());
        result.setMaxOutputTokens(request.getMax_tokens());
        result.setEndpoint(endpoint);
        return result;
    }

    private ChatCompletionResult convertResponse(ChatResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(response.getId());
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        result.setModel(this.backendConfig.getModel());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getResult());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason("stop");
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        Usage usage = new Usage();
        usage.setCompletion_tokens(response.getUsage().getCompletionTokens());
        usage.setPrompt_tokens(response.getUsage().getPromptTokens());
        usage.setTotal_tokens(response.getUsage().getTotalTokens());
        result.setUsage(usage);
        return result;
    }
}
