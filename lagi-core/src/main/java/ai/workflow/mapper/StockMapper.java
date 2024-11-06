package ai.workflow.mapper;

import ai.agent.citic.StockAgent;
import ai.mr.IMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.worker.WorkerGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class StockMapper extends CiticMapper implements IMapper {
    protected int priority;
    private static final Logger logger = LoggerFactory.getLogger(StockMapper.class);
    private final StockAgent stockAgent = new StockAgent(AGENT_CONFIG_MAP.get("ai.agent.citic.StockAgent"));

    private final String BAD_CASE =  "很抱歉，您的问题与股票无关，我无法为您提供答案。";

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
            chatCompletionResult = stockAgent.chat(chatCompletionRequest);
            if(chatCompletionResult != null) {
                positive = getSimilarity(chatCompletionRequest, chatCompletionResult);
                negative = getBadCaseSimilarity(BAD_CASE, chatCompletionResult);
                calPriority = calculatePriority(positive, negative, getPriority());
            }
        } catch (IOException e) {
            logger.error("StockMapper.myMapping: chat error", e);
        }
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
        logger.info("StockMapper.myMapping: positive = " + positive);
        logger.info("StockMapper.myMapping: negative = " + negative);
        logger.info("StockMapper.myMapping: calPriority = " + calPriority);
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
