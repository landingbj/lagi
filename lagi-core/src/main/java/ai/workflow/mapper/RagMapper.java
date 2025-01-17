package ai.workflow.mapper;

import ai.config.ContextLoader;
import ai.config.pojo.RAGFunction;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.mr.IMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.router.utils.RouteGlobal;
import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class RagMapper extends ChatAgentMapper implements IMapper {
    protected int priority;
    private static final Logger logger = LoggerFactory.getLogger(RagMapper.class);

    private final Gson gson = new Gson();

    private String agentName = "RAG";

    private  String badcase = "很抱歉";

    private final RAGFunction RAG_CONFIG = ContextLoader.configuration.getStores().getRag();


    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                RouteGlobal.MAPPER_CHAT_REQUEST);

        ChatCompletionResult chatCompletionResult = agent.communicate(chatCompletionRequest);
        double calPriority = 0;
        ChatCompletionResultWithSource chatCompletionResultWithSource;
        if (chatCompletionResult != null) {
            if(chatCompletionResult.getChoices() != null
                    && !chatCompletionResult.getChoices().isEmpty()
                    && chatCompletionResult.getChoices().get(0).getMessage().getContext() == null) {
                chatCompletionResultWithSource = new ChatCompletionResultWithSource();
            } else {
                chatCompletionResultWithSource = new ChatCompletionResultWithSource(agentName);
            }
            BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
            chatCompletionResult = chatCompletionResultWithSource;
            calPriority = calculatePriority(chatCompletionRequest, chatCompletionResult);
        }

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
        return result;
    }

    public double calculatePriority(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) {

        double positive = getSimilarity(chatCompletionRequest, chatCompletionResult);
        double negative = getBadCaseSimilarity(getBadCase(), chatCompletionResult);
        double add =  getPriorityWordPriority(chatCompletionRequest, chatCompletionResult);
        double calcPriority = positive * 5 + (negative * -5) + getPriority() + add;
        log.info("{} .myMapping: add = {}" , getAgentName(), add);
        log.info("{} .myMapping: positive = {}" , getAgentName(), positive);
        log.info("{} .myMapping: negative = {}" , getAgentName(), negative);
        log.info("{} .myMapping: calPriority = {}", getAgentName(),  calcPriority);
        return calcPriority;
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
