package ai.medusa;

import ai.medusa.consumer.CompletePromptConsumer;
import ai.medusa.exception.CompletePromptErrorHandler;
import ai.medusa.exception.DiversifyPromptErrorHandler;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.producer.*;
import ai.mr.pipeline.ProducerConsumerPipeline;
import ai.mr.pipeline.ThreadedProducerConsumerPipeline;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;

import java.util.*;

public class CompletionCache {
    private static final CompletionCache instance = new CompletionCache();
    private static final LRUCache<PromptInput, ChatCompletionResult> cache;

    private static final PromptPool promptPool = new PromptPool(PromptCacheConstant.POOL_CACHE_SIZE);
    private static ProducerConsumerPipeline<PooledPrompt> promptProcessor;
    private static ProducerConsumerPipeline<PooledPrompt> promptLoader;
    private static final DiversifyPromptProducer llmDiversifyPromptProducer = new LlmDiversifyPromptProducer(PromptCacheConstant.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer treeDiversifyPromptProducer = new TreeDiversifyPromptProducer(PromptCacheConstant.PRODUCER_LIMIT);

    static {
        cache = new LRUCache<>(PromptCacheConstant.COMPLETION_CACHE_SIZE);
    }

    private CompletionCache() {
    }

    public static CompletionCache getInstance() {
        return instance;
    }

    public ChatCompletionResult get(PromptInput promptInput) {
        return cache.get(promptInput);
    }

    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        cache.put(promptInput, chatCompletionResult);
        promptPool.put(
                PooledPrompt.builder().promptInput(promptInput).status(PromptCacheConstant.POOL_INITIAL).build()
        );
    }

    public int size() {
        return cache.size();
    }

    public PromptInput getPromptInput(ChatCompletionRequest chatCompletionRequest) {
        List<String> promptList = new ArrayList<>(2);
        List<ChatMessage> messages = chatCompletionRequest.getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
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


    public void startProcessingPrompt() {
        if (promptLoader == null) {
            promptLoader = new ThreadedProducerConsumerPipeline<>(
                    PromptCacheConstant.PRODUCER_THREADS,
                    PromptCacheConstant.CONSUMER_THREADS,
                    PromptCacheConstant.TOTAL_THREAD_COUNT,
                    Integer.MAX_VALUE
            );
            promptLoader.connectProducer(new PickPromptProducer(promptPool));
            promptLoader.connectConsumer(llmDiversifyPromptProducer);
            promptLoader.connectConsumer(treeDiversifyPromptProducer);
            promptLoader.start();
        }

        if (promptProcessor == null) {
            promptProcessor = new ThreadedProducerConsumerPipeline<>(
                    PromptCacheConstant.PRODUCER_THREADS,
                    PromptCacheConstant.CONSUMER_THREADS,
                    PromptCacheConstant.TOTAL_THREAD_COUNT,
                    Integer.MAX_VALUE
            );
            promptProcessor.connectProducer(llmDiversifyPromptProducer);
            promptProcessor.connectProducer(treeDiversifyPromptProducer);
            promptProcessor.registerProducerErrorHandler(new DiversifyPromptErrorHandler(promptPool));
            promptProcessor.connectConsumer(new CompletePromptConsumer(cache));
            promptProcessor.registerConsumerErrorHandler(new CompletePromptErrorHandler(promptPool));
            promptProcessor.start();
        }
    }
}
