package ai.worker.citic;

import ai.common.pojo.Configuration;
import ai.config.pojo.AgentConfig;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.llm.service.CompletionsService;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.prompt.PromptFactory;
import ai.qa.AiGlobalQA;
import ai.utils.OkHttpUtil;
import ai.worker.WorkerGlobal;
import ai.workflow.container.AgentContainer;
import ai.workflow.mapper.ExchangeMapper;
import ai.workflow.mapper.RagMapper;
import ai.workflow.mapper.StockMapper;
import ai.workflow.mapper.XiaoxinMapper;
import ai.workflow.reducer.AgentReducer;
import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CiticAgentWorker {
    public ChatCompletionResult process(ChatCompletionRequest chatCompletionRequest, String url) {
        ChatCompletionResult chatCompletionResult = null;
        Map<String, Object> params = new HashMap<>();
        params.put(WorkerGlobal.MAPPER_CHAT_REQUEST, chatCompletionRequest);
        params.put(WorkerGlobal.MAPPER_RAG_URL, url);

        try (IRContainer contain = new AgentContainer()) {
            IMapper ragMapper = new RagMapper();
            ragMapper.setParameters(params);
            ragMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY - 5);
            contain.registerMapper(ragMapper);

            IMapper xiaoxinMapper = new XiaoxinMapper();
            xiaoxinMapper.setParameters(params);
            xiaoxinMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(xiaoxinMapper);

            IMapper stockMapper = new StockMapper();
            stockMapper.setParameters(params);
            stockMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(stockMapper);

            IMapper exchangeMapper = new ExchangeMapper();
            exchangeMapper.setParameters(params);
            exchangeMapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
            contain.registerMapper(exchangeMapper);

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
