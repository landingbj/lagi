package ai.agent.proxy;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.worker.RouteWorker;
import io.reactivex.Observable;

public class WorkerProxyAgent extends ProxyAgent{

    protected RouteWorker routeWorker;

    public WorkerProxyAgent(RouteWorker routeWorker) {
        this.routeWorker = routeWorker;
    }

    public ChatCompletionResult communicate(ChatCompletionRequest request) {
        return routeWorker.work(request);
    }

    @Override
    public Observable<ChatCompletionResult> stream(ChatCompletionRequest data) {
        throw new UnsupportedOperationException("streaming is not supported");
    }

    @Override
    public boolean canStream() {
        return false;
    }
}
