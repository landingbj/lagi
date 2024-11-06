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

    private final String BAD_CASE = "小信最近学习了很多关于基金方面的知识，其他领域还有所欠缺，您可以尝试换个方式描述您的问题。";

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                WorkerGlobal.MAPPER_CHAT_REQUEST);
        ChatCompletionResult chatCompletionResult = null;
        double calPriority = 0;
        double positive = 0;
        double negative = 0;
        try {
            chatCompletionResult = xiaoxinAgent.chat(chatCompletionRequest);
            if(chatCompletionRequest != null) {
                positive = getSimilarity(chatCompletionRequest, chatCompletionResult);
                negative = getBadCaseSimilarity(BAD_CASE, chatCompletionResult);
                calPriority = calculatePriority(positive, negative, getPriority());
            }
        } catch (IOException e) {
            logger.error("XiaoxinMapper.myMapping: chat error", e);
        }
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
        logger.info("XiaoxinMapper.myMapping: positive = " + positive);
        logger.info("XiaoxinMapper.myMapping: negative = " + negative);
        logger.info("XiaoxinMapper.myMapping: calPriority = " + calPriority);
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
