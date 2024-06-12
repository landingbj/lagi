package ai.medusa;

import ai.llm.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CompletionCache {
    private static final CompletionCache instance = new CompletionCache();
    private static final Cache<PromptInput, ChatCompletionResult> cache;

    static {
        cache = initCache();
    }

    private CompletionCache() {
    }

    public static CompletionCache getInstance() {
        return instance;
    }

    public ChatCompletionResult get(PromptInput promptInput) {
        System.out.println(promptInput);
        System.out.println(promptInput.hashCode());
        return cache.getIfPresent(promptInput);
    }

    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        System.out.println(promptInput);
        System.out.println(promptInput.hashCode());
        cache.put(promptInput, chatCompletionResult);
    }

    public PromptInput getPromptInput(ChatCompletionRequest chatCompletionRequest) {
        List<String> promptList = new ArrayList<>(2);
        List<ChatMessage> messages = chatCompletionRequest.getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            promptList.add(message.getContent());
            if (promptList.size() == 1) {
                break;
            }
        }
        return PromptInput.builder()
                .maxTokens(chatCompletionRequest.getMax_tokens())
                .promptList(promptList)
                .temperature(chatCompletionRequest.getTemperature())
                .category(chatCompletionRequest.getCategory())
                .build();
    }

    private static Cache<PromptInput, ChatCompletionResult> initCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    public long size() {
        return cache.size();
    }
}
