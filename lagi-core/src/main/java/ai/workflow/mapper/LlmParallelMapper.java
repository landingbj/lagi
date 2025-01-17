package ai.workflow.mapper;

import ai.agent.Agent;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.router.Route;
import ai.router.pojo.RouteCompletionResult;
import ai.router.utils.RouteGlobal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Setter
@Slf4j
public class LlmParallelMapper extends BaseMapper implements IMapper {
    protected Route route;

    public LlmParallelMapper(Route route) {
        this.route = route;
    }

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest =
                (ChatCompletionRequest) this.getParameters().get(RouteGlobal.MAPPER_CHAT_REQUEST);
        RouteCompletionResult routeCompletionResult = this.route.invokeLlm(chatCompletionRequest);
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, routeCompletionResult);
        return result;
    }
}
