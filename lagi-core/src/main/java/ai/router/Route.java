package ai.router;

import ai.agent.Agent;
import ai.common.ModelService;
import ai.llm.adapter.ILlmAdapter;
import ai.manager.LlmManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import ai.router.pojo.RouteCompletionResult;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Getter
public class Route {
    protected String name;
    protected List<Function<ChatCompletionRequest, ChatCompletionResult>> handlers;

    protected static final Map<String, ILlmAdapter> llmAdapters = new ConcurrentHashMap<>();
    protected static final Map<String, Agent<ChatCompletionRequest, ChatCompletionResult>> agentHandlers = new ConcurrentHashMap<>();

    static {
        LlmManager.getInstance().getAdapters().forEach((k) -> {
            llmAdapters.put(((ModelService) k).getBackend(), k);
        });
    }

    public void putAgentHandler(Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        agentHandlers.put(agent.getAgentConfig().getName(), agent);
    }

    public Agent<ChatCompletionRequest, ChatCompletionResult> getAgentHandler(String name) {
        return agentHandlers.get(name);
    }

    public Route(String name) {
        this.name = name;
    }

    public Route(List<Route> routes) {
    }

    public RouteAgentResult invokeAgent(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        return null;
    }

    public RouteCompletionResult invokeLlm(ChatCompletionRequest request) {
        return null;
    }
}
