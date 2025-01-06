package ai.agent.proxy;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.worker.RouteWorker;

public class WorkerProxyAgent extends ProxyAgent{

    protected RouteWorker routeWorker;

    public WorkerProxyAgent(RouteWorker routeWorker) {
        this.routeWorker = routeWorker;
    }

    public ChatCompletionResult communicate(ChatCompletionRequest request) {
        return routeWorker.work(request);
    }


}
