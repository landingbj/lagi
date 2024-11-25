package ai.llm.schedule;

import ai.common.exception.RRException;
import ai.common.pojo.IndexSearchData;
import ai.common.utils.ThreadPoolManager;
import ai.config.ContextLoader;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.consumer.LlmCompletionConsumer;
import ai.llm.pojo.llmScheduleData;
import ai.llm.producer.LlmBaseTriggerProducer;
import ai.llm.service.CompletionsService;
import ai.manager.LlmManager;
import ai.mr.pipeline.ProducerConsumerPipeline;
import ai.mr.pipeline.ThreadedProducerConsumerPipeline;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Slf4j
public class QueueSchedule {


    private final LlmBaseTriggerProducer llmBaseTriggerProducer;


    public QueueSchedule() {
        CompletionsService completionsService = new CompletionsService();
        List<ILlmAdapter> adapters = LlmManager.getInstance().getAdapters();
        this.llmBaseTriggerProducer = new LlmBaseTriggerProducer(1000, 100);
        // Semaphore Total A license is obtained when the produce method is called, and a license is released when the consumer consumes it
        ProducerConsumerPipeline<llmScheduleData> chatProcessor = new ThreadedProducerConsumerPipeline<>(
                1,
                adapters.size(),
                adapters.size() + 1,
                // Semaphore Total A license is obtained when the produce method is called, and a license is released when the consumer consumes it
                adapters.size()
        );
        chatProcessor.connect(llmBaseTriggerProducer);
        LlmCompletionConsumer llmCompletionConsumer = new LlmCompletionConsumer(completionsService);
        chatProcessor.connect(llmCompletionConsumer);

        chatProcessor.registerProducerErrorHandler(e -> {
            log.error("Error in producer: ", e);
        });
        chatProcessor.registerConsumerErrorHandler(e -> {
            log.error("Error in consumer: " , e);
        });
        chatProcessor.start();

    }

    public ChatCompletionResult schedule(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        llmScheduleData data = getChatRequest(chatCompletionRequest, indexSearchDataList);
        return data.getResult();
    }

    public Observable<ChatCompletionResult> streamSchedule(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        llmScheduleData data = getChatRequest(chatCompletionRequest,  indexSearchDataList);
        return data.getStreamResult();
    }

    private llmScheduleData getChatRequest(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        llmScheduleData data = llmScheduleData.builder()
                .latch(countDownLatch)
                .indexSearchDataList(indexSearchDataList)
                .request(chatCompletionRequest)
                .build();
        llmBaseTriggerProducer.produce(data);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("queue wait result error", e);
        }
        if(data.getException() != null) {
            throw data.getException();
        }
        return data;
    }

    public static void main(String[] args) {
        ContextLoader.loadContext();
        QueueSchedule queueSchedule = new QueueSchedule();
        //mock request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("写一首五言绝句");
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        // Set the stream parameter to false
        chatCompletionRequest.setStream(false);
        ThreadPoolManager.registerExecutor("test");
        ExecutorService executor = ThreadPoolManager.getExecutor("test");
        for (int i = 0; i < 10; i++) {
            ChatCompletionRequest copy = new ChatCompletionRequest();
            BeanUtil.copyProperties(chatCompletionRequest, copy);
            executor.submit(()->{
                try {
                    ChatCompletionResult schedule = queueSchedule.schedule(copy, null);
                    System.out.println(JSONUtil.toJsonStr(schedule));
                }catch (RRException r) {
                    System.out.println("rrexception: " + r.getMsg());
                } catch (Exception e) {
                    log.error("Error: " + e.getMessage());
                }
            });
        }
    }

}
