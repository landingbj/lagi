package ai.workflow.mapper;

import ai.agent.citic.ExchangeAgent;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.mr.IMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.worker.WorkerGlobal;
import cn.hutool.core.bean.BeanUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Getter
public class ExchangeMapper extends CiticMapper implements IMapper {
    protected int priority;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeMapper.class);
    private final ExchangeAgent exchangeAgent = new ExchangeAgent(AGENT_CONFIG_MAP.get("ai.agent.citic.ExchangeAgent"));

    private  String badcase =  "很抱歉";

    private String agentName = "汇率智能体";

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                WorkerGlobal.MAPPER_CHAT_REQUEST);
        ChatCompletionResult chatCompletionResult = null;
        double calPriority = 0;
        try {
            chatCompletionResult = exchangeAgent.chat(chatCompletionRequest);
            if(chatCompletionResult != null) {
                ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource(agentName);
                BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
                chatCompletionResult = chatCompletionResultWithSource;
                calPriority = calculatePriority(chatCompletionRequest, chatCompletionResult);
            }
        } catch (IOException e) {
            logger.error("ExchangeMapper.myMapping: chat error", e);
        }
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
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
