package ai.worker;

import ai.agent.Agent;
import ai.manager.WorkerManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class DefaultWorker {

    private final WorkerManager workerManager = WorkerManager.getInstance();

    public DefaultWorker() {
    }

    public ChatCompletionResult  work(String workerName, ChatCompletionRequest request) {
        try {
            RouteWorker worker = workerManager.getRouterWorker(workerName);
            return worker.work(request);
        } catch (Exception e) {
            log.error("worker {} work error", workerName, e);
        }
        return null;
    }


    public ChatCompletionResult work(String workerName,
                                      List<Agent<ChatCompletionRequest, ChatCompletionResult>> agentList,
                                      ChatCompletionRequest request) {
        try {
            RouteWorker worker =  workerManager.getRouterWorker(workerName);
            worker = worker.clone();
            worker.setAdditionalAgents(agentList);
            return worker.work(request);
        } catch (Exception e) {
            log.error("worker {} work error", workerName, e);
        }
        return null;
    }


}
