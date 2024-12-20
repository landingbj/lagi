package ai.worker;

import ai.manager.WorkerManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DefaultWorker {

    private final WorkerManager workerManager = WorkerManager.getInstance();

    public DefaultWorker() {
    }

    public ChatCompletionResult  work(String workerName, ChatCompletionRequest request) {
        try {
            Worker<ChatCompletionRequest,ChatCompletionResult> worker =  (Worker<ChatCompletionRequest, ChatCompletionResult>)workerManager.get(workerName);
            return worker.work(request);
        } catch (Exception e) {
            log.error("worker {} work error", workerName, e);
        }
        return null;
    }


}
