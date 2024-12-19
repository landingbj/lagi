package ai.workflow.mapper;

import ai.agent.Agent;
import ai.learn.questionAnswer.KShingle;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.medusa.utils.LCS;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.WorkPriorityWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.SkillMap;
import ai.worker.WorkerGlobal;
import cn.hutool.core.bean.BeanUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Setter
@Slf4j
public class ChatAgentMapper extends BaseMapper implements IMapper {

    protected Agent<ChatCompletionRequest, ChatCompletionResult> agent;


    public String getAgentName() {
        return agent.getAgentConfig().getName();
    }

    public String getBadCase() {
        return agent.getAgentConfig().getWrongCase() == null ? "抱歉" : agent.getAgentConfig().getWrongCase();
    }

    public double getSimilarity(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) {
        KShingle kShingle = new KShingle();
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        double[] similarity = kShingle.similarity(question, answer, 2);
        return similarity[0];
    }

    public double getBadCaseSimilarity(String badCase, ChatCompletionResult chatCompletionResult) {
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        Set<String> longestCommonSubstrings = LCS.findLongestCommonSubstrings(badCase, answer, 2);
        return LCS.getLcsRatio(badCase, longestCommonSubstrings);
    }

    public double calculatePriority(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) {

        double positive = getSimilarity(chatCompletionRequest, chatCompletionResult);
        double negative = getBadCaseSimilarity(getBadCase(), chatCompletionResult);
        double add =  getPriorityWordPriority(chatCompletionRequest, chatCompletionResult);
        double calcPriority;
        if(negative > 0.8) {
            calcPriority = getPriority()  + (negative * -10);
        } else {
            calcPriority = positive * 10  + getPriority() + add;
        }
        log.info("{} .myMapping: add = {}" , getAgentName(), add);
        log.info("{} .myMapping: positive = {}" , getAgentName(), positive);
        log.info("{} .myMapping: negative = {}" , getAgentName(), negative);
        log.info("{} .myMapping: calPriority = {}", getAgentName(),  calcPriority);
        return calcPriority;
    }

    public double getPriorityWordPriority(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) {
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        boolean work = WorkPriorityWordUtil.isPriorityWord(getAgentName(), question, answer);
        if(work) return 10;
        else return 0;
    }

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                WorkerGlobal.MAPPER_CHAT_REQUEST);
        ChatCompletionResult chatCompletionResult = null;
        double calPriority = 0;
        chatCompletionResult = agent.communicate(chatCompletionRequest);
        if(chatCompletionRequest != null) {
            ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource(getAgentName());
            BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
            chatCompletionResult = chatCompletionResultWithSource;
            calPriority = calculatePriority(chatCompletionRequest, chatCompletionResult);
            try {
                SkillMap skillMap = new SkillMap();
                skillMap.saveAgentScore(agent.getAgentConfig(), ChatCompletionUtil.getLastMessage(chatCompletionRequest), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            } catch (Exception e) {
                log.error("saveAgentScore error", e);
            }
        }
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
        return result;
    }

}
