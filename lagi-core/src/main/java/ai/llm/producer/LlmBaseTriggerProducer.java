package ai.llm.producer;

import ai.common.exception.RRException;
import ai.llm.pojo.llmScheduleData;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.utils.LLMErrorConstants;
import ai.llm.utils.PollingScheduler;
import ai.mr.pipeline.Producer;
import ai.utils.LRUCache;
import ai.utils.SlidingWindowRateLimiter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class LlmBaseTriggerProducer implements Producer<llmScheduleData> {

    private LinkedBlockingDeque<llmScheduleData> queue;

    private LRUCache<String, String> ipCache;

    private SlidingWindowRateLimiter rateLimiter;

    public LlmBaseTriggerProducer(int windowSizeMs, int maxRequestsPerSecond) {
        this.rateLimiter = new SlidingWindowRateLimiter(windowSizeMs, maxRequestsPerSecond);
    }

    @Override
    public void init() {
        ipCache = new LRUCache<>(1000);
        queue = new LinkedBlockingDeque<>(1000);
    }

    @Override
    public Collection<llmScheduleData> produce() throws Exception {
        llmScheduleData top = queue.takeFirst();
        List<llmScheduleData> res = new ArrayList<>();
        res.add(top);
        return res;
    }

    private static void setModelByPolling(List<String> models, llmScheduleData top) {
        String model = PollingScheduler.schedule(models);
        top.getRequest().setModel(model);
    }

    private void setModelByIp(llmScheduleData top, List<String> models) {
        EnhanceChatCompletionRequest enhanceChatCompletionRequest =  (EnhanceChatCompletionRequest) top.getRequest();
        String cacheModel = ipCache.get(enhanceChatCompletionRequest.getIp());
        if(ipCache.get(cacheModel) != null) {
            top.getRequest().setModel(cacheModel);
        } else {
            int hash = enhanceChatCompletionRequest.getIp().hashCode();
            int index = Math.abs(hash) % models.size();
            top.getRequest().setModel(models.get(index));
        }
    }

    public void produce(llmScheduleData llmScheduleData) {
        if(!rateLimiter.allowRequest()) {
            llmScheduleData.setException(new RRException(LLMErrorConstants.RATE_LIMIT_REACHED_ERROR, "Too many requests, please try again later."));
            llmScheduleData.getLatch().countDown();
        }else {
            queue.add(llmScheduleData);
        }
    }

    @Override
    public void cleanup() {

    }
}
