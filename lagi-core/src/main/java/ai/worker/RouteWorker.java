package ai.worker;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.Route;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class RouteWorker extends Worker<ChatCompletionRequest, ChatCompletionResult> implements Cloneable{

    @Setter
    protected List<Agent<ChatCompletionRequest, ChatCompletionResult>> additionalAgents = new ArrayList<>();

    protected Route route;

    @Override
    public ChatCompletionResult work(ChatCompletionRequest data) {
        return null;
    }

    @Override
    public ChatCompletionResult call(ChatCompletionRequest data) {
        return null;
    }

    @Override
    public void notify(ChatCompletionRequest data) {

    }

    @Override
    public RouteWorker clone() throws CloneNotSupportedException {
        return (RouteWorker) super.clone();
    }
}
