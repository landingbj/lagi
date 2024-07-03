package ai.medusa;

import ai.common.utils.FastIndexList;
import ai.llm.service.CompletionsService;
import ai.medusa.impl.CompletionCache;
import ai.medusa.pojo.PromptInput;
import ai.medusa.utils.PromptCacheConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;

import java.util.List;
import java.util.Map;

public class MedusaService {
    private static final ICache<PromptInput, ChatCompletionResult> cache;
    private final CompletionsService completionsService = new CompletionsService();

    static {
        switch (PromptCacheConfig.LOCATE_ALGORITHM) {
            case "lcs":
            case "tree":
            case "vector":
            case "hash":
            default:
                cache = CompletionCache.getInstance();
        }
        if (!PromptCacheConfig.MEDUSA_ENABLE) {
            cache.startProcessingPrompt();
        }
    }

    public ChatCompletionResult get(PromptInput promptInput) {
        if (!PromptCacheConfig.MEDUSA_ENABLE) {
            return null;
        }
        return cache.get(promptInput);
    }

    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        if (!PromptCacheConfig.MEDUSA_ENABLE) {
            return;
        }

        cache.put(promptInput, chatCompletionResult);
    }

    public ChatCompletionResult locate(PromptInput promptInput) {
        if (!PromptCacheConfig.MEDUSA_ENABLE) {
            return null;
        }
        return get(promptInput);
    }

    public void load(Map<String, String> qaPair, String category) {
        if (!PromptCacheConfig.MEDUSA_ENABLE) {
            return;
        }
        for (Map.Entry<String, String> entry : qaPair.entrySet()) {
            String prompt = entry.getKey();
            String context = entry.getValue();
            prompt = ChatCompletionUtil.getPrompt(context, prompt);
            ChatCompletionRequest chatCompletionRequest = completionsService.getCompletionsRequest(prompt);
            chatCompletionRequest.setCategory(category);
            PromptInput promptInput = getPromptInput(chatCompletionRequest);
            ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
            put(promptInput, result);
        }
    }

    public PromptInput getPromptInput(ChatCompletionRequest chatCompletionRequest) {
        List<String> promptList = new FastIndexList<>();
        List<ChatMessage> messages = chatCompletionRequest.getMessages();
        for (int i = 0; i < messages.size(); i = i + 2) {
            ChatMessage message = messages.get(i);
            promptList.add(message.getContent());
        }
        return PromptInput.builder()
                .maxTokens(chatCompletionRequest.getMax_tokens())
                .promptList(promptList)
                .temperature(chatCompletionRequest.getTemperature())
                .category(chatCompletionRequest.getCategory())
                .build();
    }
}
