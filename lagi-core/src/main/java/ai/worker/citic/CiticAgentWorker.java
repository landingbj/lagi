package ai.worker.citic;

import ai.agent.citic.CiticAgent;
import ai.common.pojo.Configuration;
import ai.config.pojo.AgentConfig;
import ai.llm.service.CompletionsService;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
import ai.prompt.PromptFactory;
import ai.worker.WorkerGlobal;
import ai.workflow.container.AgentContainer;
import ai.workflow.mapper.*;
import ai.workflow.reducer.AgentReducer;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CiticAgentWorker {

    private static final List<CiticAgent> agents;

    static {
        Configuration config = LagiGlobal.getConfig();
        agents = new ArrayList<>();
        for (AgentConfig agentConfig : config.getAgents()) {
            String driver = agentConfig.getDriver();
            try {
                Class<?> clazz = Class.forName(driver);
                Constructor<?> constructor = clazz.getConstructor(AgentConfig.class);
                CiticAgent citicAgent = (CiticAgent)constructor.newInstance(agentConfig);
                agents.add(citicAgent);
            } catch (Exception e) {
                log.error("agent : {}, newInstance error {}", agentConfig, e);
            }
        }
    }

    private static List<IMapper> buildMappers(List<CiticAgent> agents) {
        return agents.stream().map(agent -> {
            CiticMapper citicMapper = new CiticMapper();
            citicMapper.setCiticAgent(agent);
            return citicMapper;
        }).collect(Collectors.toList());
    }


    public ChatCompletionResult process(ChatCompletionRequest chatCompletionRequest, String url) {
        ChatCompletionResult chatCompletionResult = null;
        Map<String, Object> params = new HashMap<>();
        params.put(WorkerGlobal.MAPPER_CHAT_REQUEST, chatCompletionRequest);
        params.put(WorkerGlobal.MAPPER_RAG_URL, url);

        List<IMapper> iMappers = buildMappers(agents);
        try (IRContainer contain = new AgentContainer()) {
            IMapper ragMapper = new RagMapper();
            ragMapper.setParameters(params);
            ragMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(ragMapper);

            for (IMapper mapper : iMappers) {
                mapper.setParameters(params);
                mapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
                contain.registerMapper(mapper);
            }

            IReducer agentReducer = new AgentReducer();
            contain.registerReducer(agentReducer);

            @SuppressWarnings("unchecked")
            List<ChatCompletionResult> resultMatrix = (List<ChatCompletionResult>) contain.Init().running();
            if (resultMatrix.get(0) != null) {
                chatCompletionResult = resultMatrix.get(0);
                System.out.println("CiticAgentWorker.process: chatCompletionResult = " + chatCompletionResult);
                String responseJson = null;
                final Gson gson = new Gson();
                PromptFactory promptFactory = new PromptFactory();
                if (promptFactory.getPromptConfig().getPrompt().getEnable()) {
                    chatCompletionRequest = promptFactory.loadPrompt(chatCompletionRequest);
                    CompletionsService completionsService = new CompletionsService();
                    ChatCompletionResult promptFormatResult = completionsService.completions(chatCompletionRequest);
                    BeanUtil.copyProperties(promptFormatResult, chatCompletionResult);
                }
            }
        }
        return chatCompletionResult;
    }
}
