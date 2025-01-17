package ai.router;

import ai.agent.Agent;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import ai.router.pojo.RouteCompletionResult;
import ai.router.utils.RouteGlobal;
import ai.workflow.container.AgentContainer;
import ai.workflow.container.LlmRouteContainer;
import ai.workflow.mapper.AgentParallelMapper;
import ai.workflow.mapper.ChatAgentMapper;
import ai.workflow.mapper.LlmParallelMapper;
import ai.workflow.reducer.AgentReducer;
import ai.workflow.reducer.LlmRouteReducer;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ParallelRoute extends Route {
    private final List<Route> routes;

    public ParallelRoute(List<Route> routes) {
        super(routes);
        this.routes = routes;
    }

    @Override
    public RouteAgentResult invokeAgent(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        Map<String, Object> params = new HashMap<>();
        params.put(RouteGlobal.MAPPER_CHAT_REQUEST, request);
        params.put(RouteGlobal.MAPPER_AGENT_LIST, agents);
        try (IRContainer contain = new AgentContainer()) {
            for (Route route : routes) {
                AgentParallelMapper mapper = new AgentParallelMapper(route);
                mapper.setParameters(params);
                contain.registerMapper(mapper);
            }
            IReducer agentReducer = new AgentReducer();
            contain.registerReducer(agentReducer);
            @SuppressWarnings("unchecked")
            List<RouteAgentResult> resultMatrix = (List<RouteAgentResult>) contain.Init().running();
            if (!resultMatrix.isEmpty()) {
                return resultMatrix.get(0);
            }
        }
        return null;
    }

    @Override
    public RouteCompletionResult invokeLlm(ChatCompletionRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put(RouteGlobal.MAPPER_CHAT_REQUEST, request);
        try (IRContainer contain = new LlmRouteContainer()) {
            for (Route route : routes) {
                LlmParallelMapper mapper = new LlmParallelMapper(route);
                mapper.setParameters(params);
                contain.registerMapper(mapper);
            }
            IReducer reducer = new LlmRouteReducer();
            contain.registerReducer(reducer);
            @SuppressWarnings("unchecked")
            List<RouteCompletionResult> resultMatrix = (List<RouteCompletionResult>) contain.Init().running();
            if (!resultMatrix.isEmpty()) {
                return resultMatrix.get(0);
            }
        }
        return null;
    }
}
