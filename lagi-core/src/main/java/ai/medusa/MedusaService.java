package ai.medusa;

import ai.common.utils.FastIndexList;
import ai.llm.service.CompletionsService;
import ai.medusa.impl.CompletionCache;
import ai.medusa.pojo.PromptInput;
import ai.medusa.pojo.PromptParameter;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.utils.PromptPool;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;

import java.util.List;
import java.util.Map;

public class MedusaService {
    private static ICache<PromptInput, ChatCompletionResult> cache;
    private final CompletionsService completionsService = new CompletionsService();

    static {
        if (PromptCacheConfig.MEDUSA_ENABLE && LagiGlobal.RAG_ENABLE) {
            switch (PromptCacheConfig.LOCATE_ALGORITHM) {
                case "lcs":
                case "tree":
                case "vector":
                case "hash":
                default:
                    cache = CompletionCache.getInstance();
            }
            cache.startProcessingPrompt();
        }
    }

    public ChatCompletionResult get(PromptInput promptInput) {
        if (cache == null) {
            return null;
        }
        return cache.get(promptInput);
    }

    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        if (cache == null) {
            return;
        }
        cache.put(promptInput, chatCompletionResult);
    }

    public void triggerCachePut(PromptInput promptInput) {
        if (cache == null) {
            return;
        }
        cache.put(promptInput, null);
    }

    public ChatCompletionResult locate(PromptInput promptInput) {
        if (cache == null) {
            return null;
        }
        return get(promptInput);
    }

    public void load(Map<String, String> qaPair, String category) {
        if (cache == null) {
            return;
        }
        for (Map.Entry<String, String> entry : qaPair.entrySet()) {
            String prompt = entry.getKey();
            ChatCompletionRequest chatCompletionRequest = completionsService.getCompletionsRequest(prompt);
            chatCompletionRequest.setCategory(category);
            PromptInput promptInput = getPromptInput(chatCompletionRequest);
            triggerCachePut(promptInput);
        }
    }

    public PromptPool getPromptPool() {
        if (cache == null) {
            return null;
        }
        return cache.getPromptPool();
    }

    public PromptInput getPromptInput(ChatCompletionRequest chatCompletionRequest) {
        List<String> promptList = new FastIndexList<>();
        List<ChatMessage> messages = chatCompletionRequest.getMessages();
        String systemPrompt = null;
        for (ChatMessage message : messages) {
            if (message.getRole().equals(LagiGlobal.LLM_ROLE_USER)) {
                promptList.add(message.getContent());
            } else if (message.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                systemPrompt = message.getContent();
            }
        }
        PromptParameter parameter = PromptParameter.builder()
                .maxTokens(chatCompletionRequest.getMax_tokens())
                .temperature(chatCompletionRequest.getTemperature())
                .category(chatCompletionRequest.getCategory())
                .systemPrompt(systemPrompt)
                .build();
        return PromptInput.builder()
                .promptList(promptList)
                .parameter(parameter)
                .build();
    }
}
