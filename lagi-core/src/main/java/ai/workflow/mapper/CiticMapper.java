package ai.workflow.mapper;

import ai.common.pojo.Configuration;
import ai.config.pojo.AgentConfig;
import ai.learn.questionAnswer.KShingle;
import ai.medusa.utils.LCS;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
import ai.utils.WorkPriorityWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Getter
@Slf4j
public class CiticMapper extends BaseMapper {
    protected static final Map<String, AgentConfig> AGENT_CONFIG_MAP = new HashMap<>();

    private  String agentName = "citic";

    private  String badcase = "抱歉, 我并不了解xxx具体的信息,分享更多关于xxx的信息";

    static {
        Configuration config = LagiGlobal.getConfig();
        for (AgentConfig agentConfig : config.getAgents()) {
            AGENT_CONFIG_MAP.put(agentConfig.getDriver(), agentConfig);
        }
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
        double negative = getBadCaseSimilarity(getBadcase(), chatCompletionResult);
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

}
