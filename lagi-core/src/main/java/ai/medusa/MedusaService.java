package ai.medusa;

import ai.common.pojo.Medusa;
import ai.common.utils.FastIndexList;
import ai.config.ContextLoader;
import ai.config.GlobalConfigurations;
import ai.llm.service.CompletionsService;
import ai.medusa.impl.CompletionCache;
import ai.medusa.pojo.CacheItem;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.pojo.PromptParameter;
import ai.medusa.utils.CacheLoader;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.utils.PromptPool;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.vector.VectorStoreService;
import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MedusaService {
    private static ICache<PromptInput, ChatCompletionResult> cache;
    private final CompletionsService completionsService = new CompletionsService();

    static {
        if (PromptCacheConfig.MEDUSA_ENABLE) {
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

    public void init() {
        CompletableFuture.runAsync(() -> {
            try {
                GlobalConfigurations configuration = ContextLoader.configuration;
                if(configuration != null && configuration.getStores() != null && configuration.getStores().getMedusa() != null) {
                    loadCacheFromFile();
                    Medusa medusa = configuration.getStores().getMedusa();
                    String inits = medusa.getInits();
                    preloadPrompts(inits);
                }
            }catch (Exception ignored){}
        });
    }

    private void loadCacheFromFile() {
        if (cache == null) {
            return;
        }
        CacheLoader cacheLoader = CacheLoader.getInstance();
        cacheLoader.loadFromFiles();
        List<CacheItem> cacheItems = cacheLoader.getLoadedItems();
        for (CacheItem cacheItem : cacheItems) {
            PromptInput promptInput = cacheItem.getPromptInput();
            ChatCompletionResult chatCompletionRequest = cacheItem.getChatCompletionResult();
            cache.put(promptInput, chatCompletionRequest, false, PromptCacheConfig.MEDUSA_FLUSH);
        }
        cacheLoader.clearLoadedItems();
    }

    private void preloadPrompts(String inits) {
        if(!StrUtil.isBlank(inits)) {
            String[] prePrompts = inits.split(",");
            VectorStoreService vectorStoreService = new VectorStoreService();
            for (String prePrompt : prePrompts) {
                prePrompt = prePrompt.trim();
                ChatCompletionRequest chatCompletionRequest = completionsService.getCompletionsRequest(prePrompt);
                chatCompletionRequest.setCategory(vectorStoreService.getVectorStoreConfig().getDefaultCategory());
                PromptInput promptInput = getPromptInput(chatCompletionRequest);
                triggerCachePut(promptInput);
                if (getPromptPool() != null) {
                    getPromptPool().put(PooledPrompt.builder()
                            .promptInput(promptInput).status(PromptCacheConfig.POOL_INITIAL).build());
                }
            }
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

    public void triggerCachePutAndDiversify(PromptInput promptInput) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000);
            }catch (InterruptedException ignored){}
            if (this.getPromptPool() != null) {
                this.getPromptPool().put(PooledPrompt.builder()
                        .promptInput(promptInput).status(PromptCacheConfig.POOL_INITIAL).build());
            }

        });
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
