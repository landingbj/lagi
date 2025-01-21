package ai.router;

import ai.agent.Agent;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import ai.router.pojo.RouteCompletionResult;
import ai.router.utils.RouteGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.skillMap.SkillMap;
import ai.workflow.container.AgentContainer;
import ai.workflow.container.LlmRouteContainer;
import ai.workflow.mapper.AgentParallelMapper;
import ai.workflow.mapper.LlmParallelMapper;
import ai.workflow.reducer.AgentReducer;
import ai.workflow.reducer.LlmRouteReducer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ParallelRoute extends Route {

    public ParallelRoute(List<Route> routes) {
        super(routes);
        this.routes = routes;
//        System.out.println("ParallelRoute : " + routes);
    }

    protected List<Agent<ChatCompletionRequest, ChatCompletionResult>> filterAgentsBySkillMap(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents, ChatCompletionRequest data) {
        SkillMap skillMap = new SkillMap();
        return skillMap.filterAgentByIntentKeyword(agents, ChatCompletionUtil.getLastMessage(data), 5.0);
    }


    @Override
    public RouteAgentResult invokeAgent(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        Map<String, Object> params = new HashMap<>();
        params.put(RouteGlobal.MAPPER_CHAT_REQUEST, request);
        params.put(RouteGlobal.MAPPER_AGENT_LIST, agents);
        try (IRContainer contain = new AgentContainer()) {
            // has online agent
            List<Route> toMapperRoutes = getFilterMappers(request, agents);
            for (Route route : toMapperRoutes) {
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

    private List<Route> getFilterMappers(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        // TODO 2025/1/21 remove to best work example: use function code function<A,B>
        long count = routes.stream().filter(route -> route instanceof BasicRoute).count();
        List<Route> toMapperRoutes;
        if(count == (long)routes.size()) {
            toMapperRoutes = new ArrayList<>();
            List<Agent<ChatCompletionRequest, ChatCompletionResult>> configAgents = routes.stream()
                    .map(route -> route.getAgent()).collect(Collectors.toList());
            agents.addAll(configAgents);
            agents = filterAgentsBySkillMap(agents, request);
            agents.forEach(agent -> {
                BasicRoute basicRoute = new BasicRoute(agent);
                toMapperRoutes.add(basicRoute);
            });
        } else {
            toMapperRoutes = routes;
        }
        return toMapperRoutes;
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
