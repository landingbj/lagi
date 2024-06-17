package ai.medusa.impl;

import ai.medusa.ICache;
import ai.medusa.PromptCacheConfig;
import ai.medusa.PromptPool;
import ai.medusa.consumer.CompletePromptConsumer;
import ai.medusa.exception.CompletePromptErrorHandler;
import ai.medusa.exception.DiversifyPromptErrorHandler;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.producer.*;
import ai.mr.pipeline.ProducerConsumerPipeline;
import ai.mr.pipeline.ThreadedProducerConsumerPipeline;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LRUCache;

import java.util.Map;


public class CompletionCache implements ICache {
    private static final CompletionCache instance = new CompletionCache();
    private static final LRUCache<PromptInput, ChatCompletionResult> cache;

    private static final PromptPool promptPool = new PromptPool(PromptCacheConfig.POOL_CACHE_SIZE);
    private static ProducerConsumerPipeline<PooledPrompt> promptProcessor;
    private static ProducerConsumerPipeline<PooledPrompt> promptLoader;
    private static final DiversifyPromptProducer llmDiversifyPromptProducer = new LlmDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer treeDiversifyPromptProducer = new TreeDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer ragDiversifyPromptProducer = new RagDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);

    static {
        cache = new LRUCache<>(PromptCacheConfig.COMPLETION_CACHE_SIZE);
    }

    private CompletionCache() {
    }

    public static CompletionCache getInstance() {
        return instance;
    }

    @Override
    public ChatCompletionResult get(PromptInput promptInput) {
        return cache.get(promptInput);
    }

    @Override
    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        cache.put(promptInput, chatCompletionResult);
        this.getPromptPool().put(PooledPrompt.builder()
                .promptInput(promptInput).status(PromptCacheConfig.POOL_INITIAL).build());
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public ChatCompletionResult locate(PromptInput promptInput) {
        return get(promptInput);
    }

    @Override
    public PromptPool getPromptPool() {
        return promptPool;
    }

    @Override
    public void startProcessingPrompt() {
        if (promptLoader == null) {
            promptLoader = new ThreadedProducerConsumerPipeline<>(
                    PromptCacheConfig.PRODUCER_THREADS,
                    PromptCacheConfig.CONSUMER_THREADS,
                    PromptCacheConfig.TOTAL_THREAD_COUNT,
                    Integer.MAX_VALUE
            );
            promptLoader.connectProducer(new PickPromptProducer(promptPool));
            promptLoader.connectConsumer(llmDiversifyPromptProducer);
            promptLoader.connectConsumer(treeDiversifyPromptProducer);
            promptLoader.connectConsumer(ragDiversifyPromptProducer);
            promptLoader.start();
        }

        if (promptProcessor == null) {
            promptProcessor = new ThreadedProducerConsumerPipeline<>(
                    PromptCacheConfig.PRODUCER_THREADS,
                    PromptCacheConfig.CONSUMER_THREADS,
                    PromptCacheConfig.TOTAL_THREAD_COUNT,
                    Integer.MAX_VALUE
            );
            promptProcessor.connectProducer(llmDiversifyPromptProducer);
            promptProcessor.connectProducer(treeDiversifyPromptProducer);
            promptProcessor.connectProducer(ragDiversifyPromptProducer);
            promptProcessor.registerProducerErrorHandler(new DiversifyPromptErrorHandler(promptPool));
            promptProcessor.connectConsumer(new CompletePromptConsumer(cache));
            promptProcessor.registerConsumerErrorHandler(new CompletePromptErrorHandler(promptPool));
            promptProcessor.start();
        }
    }
}
