package ai.workflow.mapper;

import ai.agent.Agent;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.router.Route;
import ai.router.pojo.RouteAgentResult;
import ai.router.utils.RouteGlobal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Setter
@Slf4j
public class AgentParallelMapper extends BaseMapper implements IMapper {

    protected Agent<ChatCompletionRequest, ChatCompletionResult> agent;
    protected Route route;

    public AgentParallelMapper(Route route) {
        this.route = route;
    }

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                RouteGlobal.MAPPER_CHAT_REQUEST);
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents =
                (List<Agent<ChatCompletionRequest, ChatCompletionResult>>) this.getParameters().get(
                RouteGlobal.MAPPER_AGENT_LIST);
        RouteAgentResult routeCompletionResult = this.route.invokeAgent(chatCompletionRequest, agents);
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, routeCompletionResult);
        return result;
    }
}
