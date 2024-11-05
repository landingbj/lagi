package ai.workflow.mapper;

import ai.agent.citic.StockAgent;
import ai.agent.citic.XiaoxinAgent;
import ai.common.pojo.Backend;
import ai.config.pojo.AgentConfig;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.WorkerGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XiaoxinMapper extends CiticMapper implements IMapper {
    protected int priority;
    private static final Logger logger = LoggerFactory.getLogger(XiaoxinMapper.class);
    private final XiaoxinAgent xiaoxinAgent = new XiaoxinAgent(AGENT_CONFIG_MAP.get("ai.agent.citic.XiaoxinAgent"));

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                WorkerGlobal.MAPPER_CHAT_REQUEST);
        ChatCompletionResult chatCompletionResult = null;
        double similarity = -1;
        try {
            chatCompletionResult = xiaoxinAgent.chat(chatCompletionRequest);
            similarity = getSimilarity(chatCompletionRequest, chatCompletionResult);
        } catch (IOException e) {
            logger.error("XiaoxinMapper.myMapping: chat error", e);
        }
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority() * similarity);
        return result;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
