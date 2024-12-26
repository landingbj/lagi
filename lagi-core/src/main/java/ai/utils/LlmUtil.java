package ai.utils;

import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class LlmUtil {

    public static CompletionsService completionsService = new CompletionsService();

    public static ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(prompt);
        systemMessage.setRole("system");
        chatMessages.add(systemMessage);
        for (int i = 0; i < history.size(); i++) {
            ChatMessage userMessage = new ChatMessage();
            userMessage.setRole("user");
            userMessage.setContent(history.get(i).get(0));

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(history.get(i).get(1));

            chatMessages.add(userMessage);
            chatMessages.add(assistantMessage);
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(userMsg);
        chatMessages.add(userMessage);
        request.setMax_tokens(1024);
        request.setTemperature(0);
        request.setMessages(chatMessages);
        return completionsService.completions(request);
    }
}
