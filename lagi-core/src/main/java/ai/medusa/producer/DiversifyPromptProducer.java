package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.medusa.PromptCacheConfig;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.mr.pipeline.ConnectedProducerConsumerPipeline;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import ai.utils.PriorityWordUtil;
import ai.vector.VectorStoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DiversifyPromptProducer extends ConnectedProducerConsumerPipeline<PooledPrompt, PooledPrompt> {
    private static final LRUCache<ChatCompletionRequest, List<IndexSearchData>> cache = new LRUCache<>(PromptCacheConfig.COMPLETION_CACHE_SIZE);
    private final VectorStoreService vectorStoreService = new VectorStoreService();

    public DiversifyPromptProducer(int limit) {
        super(limit);
    }

    protected List<IndexSearchData> searchByContext(PromptInput promptInput) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setTemperature(promptInput.getTemperature());
        request.setMax_tokens(promptInput.getMaxTokens());
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_SYSTEM);
        message.setContent(promptInput.getPromptList().get(promptInput.getPromptList().size() - 1));
        messages.add(message);
        request.setMessages(messages);
        return searchByContext(request);
    }

    protected List<IndexSearchData> searchByContext(ChatCompletionRequest request) {
        List<IndexSearchData> result;
        if (cache.containsKey(request)) {
            result = cache.get(request);
        } else {
            List<IndexSearchData> search = vectorStoreService.searchByContext(request);
            result = PriorityWordUtil.sortByPriorityWord(search);
            cache.put(request, result);
        }
        return result;
    }

    protected List<IndexSearchData> search(String question, String category) {
        return vectorStoreService.search(question, 30, 0.01, new HashMap<>(), category);
    }

    protected IndexSearchData getParentIndex(String parentId, String category) {
        return vectorStoreService.getParentIndex(parentId, category);
    }
}
