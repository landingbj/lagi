package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.config.pojo.RAGFunction;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.mr.pipeline.ConnectedProducerConsumerPipeline;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import ai.utils.PriorityWordUtil;
import ai.vector.VectorStoreService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

@Slf4j
public abstract class DiversifyPromptProducer extends ConnectedProducerConsumerPipeline<PooledPrompt, PooledPrompt> {
    protected final RAGFunction RAG_CONFIG = ContextLoader.configuration.getStores().getRag();
    private static final LRUCache<ChatCompletionRequest, List<IndexSearchData>> cache = new LRUCache<>(PromptCacheConfig.COMPLETION_CACHE_SIZE);
    private final VectorStoreService vectorStoreService = new VectorStoreService();

    protected static final Semaphore semaphore = new Semaphore(1);

    public DiversifyPromptProducer(int limit) {
        super(limit);
    }

    protected List<IndexSearchData> searchByContext(PromptInput promptInput) {
        if (promptInput.getParameter().getCategory() == null) {
            return Collections.emptyList();
        }
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setTemperature(promptInput.getParameter().getTemperature());
        request.setMax_tokens(promptInput.getParameter().getMaxTokens());
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_SYSTEM);
        message.setContent(promptInput.getPromptList().get(promptInput.getPromptList().size() - 1));
        messages.add(message);
        request.setMessages(messages);
        return searchByContext(request);
    }

    protected List<IndexSearchData> searchByContext(ChatCompletionRequest request) {
        List<IndexSearchData> result = Collections.emptyList();
        if (cache.containsKey(request)) {
            result = cache.get(request);
        } else {
            try {
                semaphore.acquire();
                try {
                    List<IndexSearchData> search = vectorStoreService.searchByContext(request);
                    result = PriorityWordUtil.sortByPriorityWord(search);
                    cache.put(request, result);
                }catch (Exception e) {
                    log.error(" diversify Failed to search for question: {}",  request);
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException e) {
                log.error("diversify Failed to acquire semaphore", e);
            }
        }
        return result;
    }

    protected List<IndexSearchData> search(String question, String category) {
        return vectorStoreService.search(question, 30, 0.2, new HashMap<>(), category);
    }

    protected IndexSearchData getParentIndex(String parentId, String category) {
        return vectorStoreService.getParentIndex(parentId, category);
    }

    protected IndexSearchData getChildIndex(String parentId, String category) {
        return vectorStoreService.getChildIndex(parentId, category);
    }
}
