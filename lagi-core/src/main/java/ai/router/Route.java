package ai.router;

import ai.agent.Agent;
import ai.common.ModelService;
import ai.llm.adapter.ILlmAdapter;
import ai.manager.AgentManager;
import ai.manager.LlmManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import ai.router.pojo.RouteCompletionResult;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Data
@Getter
public class Route implements Serializable {

    protected List<Route> routes;
    protected String name;
    protected List<Function<ChatCompletionRequest, ChatCompletionResult>> handlers;
    @Getter
    protected Agent<ChatCompletionRequest, ChatCompletionResult> agent;
    protected static final Map<String, ILlmAdapter> llmAdapters = new ConcurrentHashMap<>();
    protected static final Map<String, Agent<ChatCompletionRequest, ChatCompletionResult>> agentHandlers = new ConcurrentHashMap<>();

    static {
        LlmManager.getInstance().getAdapters().forEach((k) -> {
            llmAdapters.put(((ModelService) k).getModel(), k);
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
        Agent<?, ?> agent = AgentManager.getInstance().get(name);
        if(agent != null) {
            this.agent = (Agent<ChatCompletionRequest, ChatCompletionResult>)agent;
            putAgentHandler(this.agent);
        }
    }

    public Route(Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        this.agent = agent;
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
