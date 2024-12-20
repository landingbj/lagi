package ai.router;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public class Route {
    protected String name;
    protected List<Function<ChatCompletionRequest, ChatCompletionResult>> handlers;

    public Route(String name) {
        this.name = name;
    }

    public List<ChatCompletionResult> invoke(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        return null;
    }

}
