package ai.medusa.impl;

import ai.medusa.ICache;
import ai.medusa.utils.*;
import ai.medusa.consumer.CompletePromptConsumer;
import ai.medusa.exception.CompletePromptErrorHandler;
import ai.medusa.exception.DiversifyPromptErrorHandler;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.producer.*;
import ai.mr.pipeline.ProducerConsumerPipeline;
import ai.mr.pipeline.ThreadedProducerConsumerPipeline;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ContinueWordUtil;
import ai.utils.LRUCache;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;


public class CompletionCache implements ICache<PromptInput, ChatCompletionResult> {
    @Getter
    private static final CompletionCache instance = new CompletionCache();
    private static final LRUCache<PromptInput, List<ChatCompletionResult>> promptCache;
    private static final QaCache qaCache = new QaCache();

    private static final PromptPool promptPool = new PromptPool(PromptCacheConfig.POOL_CACHE_SIZE);
    private static ProducerConsumerPipeline<PooledPrompt> promptProcessor;
    private static ProducerConsumerPipeline<PooledPrompt> promptLoader;
    private static final DiversifyPromptProducer llmDiversifyPromptProducer = new LlmDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer treeDiversifyPromptProducer = new TreeDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer ragDiversifyPromptProducer = new RagDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer pageDiversifyPromptProducer = new PageDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);
    private static final DiversifyPromptProducer reasonDiversifyPromptProducer = new ReasonDiversifyPromptProducer(PromptCacheConfig.PRODUCER_LIMIT);

    private static final int SUBSTRING_THRESHOLD = PromptCacheConfig.SUBSTRING_THRESHOLD;
    private static final double LCS_RATIO_PROMPT_INPUT = PromptCacheConfig.LCS_RATIO_PROMPT_INPUT;

    static {
        promptCache = new LRUCache<>(PromptCacheConfig.COMPLETION_CACHE_SIZE);
    }

    private CompletionCache() {
    }

    @Override
    public ChatCompletionResult get(PromptInput promptInput) {
        return pickCompletionResult(promptInput);
    }

    @Override
    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        put(promptInput, chatCompletionResult, true, true);
    }

    @Override
    public void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult, boolean needPersistent, boolean flush) {
        new PromptCacheTrigger(this).triggerWriteCache(promptInput, chatCompletionResult, needPersistent, flush);
    }

    @Override
    public void put(PromptInput promptInput) {
        put(promptInput, null);
    }

    public ChatCompletionResult pickCompletionResult(PromptInput promptInput) {
        if (promptInput == null) {
            return null;
        }
        String newestPrompt = PromptInputUtil.getNewestPrompt(promptInput);
        List<PromptInput> promptInputList = qaCache.get(newestPrompt);
        if (promptInputList == null) {
            newestPrompt = qaCache.getPromptInVectorDb(newestPrompt);
            if (newestPrompt != null) {
                promptInputList = qaCache.get(newestPrompt);
            }
        }
        PromptInput pickedPromptInput = null;
        double maxRatio = 0;
        if (promptInputList != null) {
            for (PromptInput promptInputInCache : promptInputList) {
                if (!promptInput.getParameter().equals(promptInputInCache.getParameter())) {
                    continue;
                }
                List<String> promptListInCache = promptInputInCache.getPromptList();
                int index = promptListInCache.indexOf(newestPrompt);
                List<String> curPromptList = promptInput.getPromptList();
                List<String> promptList1;
                List<String> promptList2;
                if (index == 0) {
                    if (curPromptList.size() > 1
                            && ContinueWordUtil.containsStoppingWorlds(curPromptList.get(curPromptList.size() - 1))) {
                        continue;
                    }
                    promptList1 = Lists.newArrayList(promptListInCache.get(0));
                    promptList2 = Lists.newArrayList(curPromptList.get(curPromptList.size() - 1));
                } else {
                    promptList1 = Lists.newArrayList(promptListInCache.get(0), promptListInCache.get(index));
                    promptList2 = Lists.newArrayList(curPromptList.get(0), curPromptList.get(curPromptList.size() - 1));
                }
                double ratio = LCS.getLcsRatio(promptList1, promptList2, SUBSTRING_THRESHOLD);

                if (ratio > maxRatio) {
                    maxRatio = ratio;
                    pickedPromptInput = promptInputInCache;
                }
            }
        }
        if (maxRatio < LCS_RATIO_PROMPT_INPUT) {
            pickedPromptInput = null;
        }
        ChatCompletionResult result = null;

        if (pickedPromptInput != null) {
            List<ChatCompletionResult> resultListInCache = promptCache.get(pickedPromptInput);
            int index = pickedPromptInput.getPromptList().indexOf(newestPrompt);
            if (index > -1 && index < resultListInCache.size()) {
                result = resultListInCache.get(index);
            }
        }
        return result;
    }

    @Override
    public int size() {
        return promptCache.size();
    }

    @Override
    public ChatCompletionResult locate(PromptInput promptInput) {
        return get(promptInput);
    }

    @Override
    public PromptPool getPromptPool() {
        return promptPool;
    }

    public LRUCache<PromptInput, List<ChatCompletionResult>> getPromptCache() {
        return promptCache;
    }

    public QaCache getQaCache() {
        return qaCache;
    }

    @Override
    public void startProcessingPrompt() {
        if (promptLoader == null) {
            promptLoader = new ThreadedProducerConsumerPipeline<>(
                    PromptCacheConfig.PRODUCER_THREADS,
                    PromptCacheConfig.CONSUMER_THREADS,
                    PromptCacheConfig.TOTAL_THREAD_COUNT,
                    PromptCacheConfig.THREAD_RUN_LIMIT
            );
            promptLoader.connectProducer(new PickPromptProducer(promptPool));
            if (PromptCacheConfig.getEnableLlmDriver()) {
                promptLoader.connectConsumer(llmDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnableTreeDriver()) {
                promptLoader.connectConsumer(treeDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnableRagDriver()) {
                promptLoader.connectConsumer(ragDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnablePageDriver()) {
                promptLoader.connectConsumer(pageDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnableReasonDriver()) {
                promptLoader.connectConsumer(reasonDiversifyPromptProducer);
            }
            promptLoader.start();
        }

        if (promptProcessor == null) {
            promptProcessor = new ThreadedProducerConsumerPipeline<>(
                    PromptCacheConfig.PRODUCER_THREADS,
                    PromptCacheConfig.CONSUMER_THREADS,
                    PromptCacheConfig.TOTAL_THREAD_COUNT,
                    PromptCacheConfig.THREAD_RUN_LIMIT
            );

            if (PromptCacheConfig.getEnableLlmDriver()) {
                promptProcessor.connectProducer(llmDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnableTreeDriver()) {
                promptProcessor.connectProducer(treeDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnableRagDriver()) {
                promptProcessor.connectProducer(ragDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnablePageDriver()) {
                promptProcessor.connectProducer(pageDiversifyPromptProducer);
            }
            if (PromptCacheConfig.getEnableReasonDriver()) {
                promptProcessor.connectProducer(reasonDiversifyPromptProducer);
            }
            promptProcessor.registerProducerErrorHandler(new DiversifyPromptErrorHandler(promptPool));
            promptProcessor.connectConsumer(new CompletePromptConsumer(CompletionCache.getInstance()));
            promptProcessor.registerConsumerErrorHandler(new CompletePromptErrorHandler(promptPool));
            promptProcessor.start();
        }
    }
}
