package ai.workflow.mapper;

import ai.agent.chat.XiaoxinAgent;
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
public class XiaoxinMapper extends ChatAgentMapper implements IMapper {
    protected int priority;
    private static final Logger logger = LoggerFactory.getLogger(XiaoxinMapper.class);
    private final XiaoxinAgent xiaoxinAgent = new XiaoxinAgent(AGENT_CONFIG_MAP.get("ai.agent.citic.XiaoxinAgent"));

    private String badcase = "小信最近学习了";

    private String agentName = "小信智能体";

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                WorkerGlobal.MAPPER_CHAT_REQUEST);
        ChatCompletionResult chatCompletionResult = null;
        double calPriority = 0;
        try {
            chatCompletionResult = xiaoxinAgent.chat(chatCompletionRequest);
            if(chatCompletionRequest != null) {
                ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource(agentName);
                BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
                chatCompletionResult = chatCompletionResultWithSource;
                calPriority = calculatePriority(chatCompletionRequest, chatCompletionResult);
            }
        } catch (IOException e) {
            logger.error("XiaoxinMapper.myMapping: chat error", e);
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
