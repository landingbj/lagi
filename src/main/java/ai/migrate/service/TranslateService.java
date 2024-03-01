package ai.migrate.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;

public class TranslateService {
    private AiService aiService = new AiService();
    
    private static final int MAX_TOKENS = 500;
    private static final double TEMPERATURE = 0.8d;

    public String enToChinese(String enStr) throws IOException {
        ChatCompletionRequest data = new ChatCompletionRequest();
        ChatMessage message = new ChatMessage();
        message.setContent("把下面的英文翻译成中文：\n" + enStr);
        message.setRole("user");
        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(message);
        data.setMax_tokens(MAX_TOKENS);
        data.setTemperature(TEMPERATURE);
        data.setMessages(messageList);
        ChatCompletionResult result = aiService.gptCompletions(data);
        String zhStr = result.getChoices().get(0).getMessage().getContent();
        return zhStr;
    }
    
    public String zhToEnglish(String zhStr) throws IOException {
        ChatCompletionRequest data = new ChatCompletionRequest();
        ChatMessage message = new ChatMessage();
        message.setContent("Translate following sentence into English:\n" + zhStr);
        message.setRole("user");
        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(message);
        data.setMax_tokens(MAX_TOKENS);
        data.setTemperature(TEMPERATURE);
        data.setMessages(messageList);
        ChatCompletionResult result = aiService.gptCompletions(data);
        String enStr = result.getChoices().get(0).getMessage().getContent();
        return enStr;
    }
}
