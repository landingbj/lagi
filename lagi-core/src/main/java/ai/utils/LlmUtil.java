package ai.utils;

import ai.config.ContextLoader;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import cn.hutool.core.bean.BeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LlmUtil {

    public static CompletionsService completionsService = new CompletionsService();

    public static ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(ContextLoader.configuration.getAgentGeneralConfiguration().getModel());
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
        request.setTemperature(0.1);
        request.setMessages(chatMessages);
        return completionsService.completions(request);
    }


    public static ChatCompletionResult callLLm(String prompt, ChatCompletionRequest req) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(ContextLoader.configuration.getAgentGeneralConfiguration().getModel());
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(prompt);
        systemMessage.setRole("system");
        chatMessages.add(systemMessage);
        List<ChatMessage> temp = req.getMessages().stream().map(chatMessage -> {
            if (!chatMessage.getRole().equals("user")) {
                ChatMessage chatMessage1 = new ChatMessage();
                BeanUtil.copyProperties(chatMessage, chatMessage1);
                chatMessage1.setContent("\n\n");
                return chatMessage1;
            }
            return chatMessage;
        }).collect(Collectors.toList());
        chatMessages.addAll(temp);
        request.setMax_tokens(1024);
        request.setTemperature(0.1);
        request.setMessages(chatMessages);
        return completionsService.completions(request);
    }
}
