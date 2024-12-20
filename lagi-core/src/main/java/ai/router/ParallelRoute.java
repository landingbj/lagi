package ai.router;

import ai.agent.Agent;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.worker.WorkerGlobal;
import ai.workflow.container.AgentContainer;
import ai.workflow.mapper.ChatAgentMapper;
import ai.workflow.reducer.AgentReducer;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ParallelRoute extends Route{

    public ParallelRoute(String name) {
        super(name);
    }

    private List<IMapper> convert2Mapper(List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        return agents.stream().map(agent -> {
            ChatAgentMapper chatAgentMapper = new ChatAgentMapper();
            chatAgentMapper.setAgent(agent);
            return chatAgentMapper;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChatCompletionResult> invoke(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        Map<String, Object> params = new HashMap<>();
        params.put(WorkerGlobal.MAPPER_CHAT_REQUEST, request);
        try (IRContainer contain = new AgentContainer()) {
            for (IMapper mapper : convert2Mapper(agents)) {
                mapper.setParameters(params);
                mapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
                contain.registerMapper(mapper);
            }
            IReducer agentReducer = new AgentReducer();
            contain.registerReducer(agentReducer);
            @SuppressWarnings("unchecked")
            List<ChatCompletionResult> resultMatrix = (List<ChatCompletionResult>) contain.Init().running();
            return resultMatrix;
        }
    }
}
