package ai.workflow.mapper;

import ai.agent.Agent;
import ai.agent.proxy.LlmProxyAgent;
import ai.agent.proxy.ProxyAgent;
import ai.common.utils.ThreadPoolManager;
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
import ai.worker.pojo.AgentIntentScore;
import ai.worker.pojo.IntentResponse;
import ai.worker.skillMap.SkillMap;
import ai.worker.WorkerGlobal;
import cn.hutool.core.bean.BeanUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;


@Setter
@Slf4j
public class ChatAgentMapper extends BaseMapper implements IMapper {

    protected Agent<ChatCompletionRequest, ChatCompletionResult> agent;

    private static final ExecutorService executorService;
    static {
        ThreadPoolManager.registerExecutor("feeScoring");
        executorService = ThreadPoolManager.getExecutor("feeScoring");
    }

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
        Boolean isFeeRequired = Boolean.TRUE.equals(agent.getAgentConfig().getIsFeeRequired());
        Boolean canOutPut = Boolean.TRUE.equals(agent.getAgentConfig().getCanOutPut());
        if((!isFeeRequired) || canOutPut) {
            // free
            chatCompletionResult = agent.communicate(chatCompletionRequest);
        } else {
            // not free detect
            SkillMap skillMap = new SkillMap();
            IntentResponse intentResponse = skillMap.intentDetect(ChatCompletionUtil.getLastMessage(chatCompletionRequest));
            List<String> keywords = intentResponse.getKeywords();
            AgentIntentScore agentIntentScore = skillMap.agentIntentScore(agent.getAgentConfig().getId(), keywords);
            // if not scoring
            if(agentIntentScore == null) {
                feeAgentScoring(chatCompletionRequest, skillMap, keywords);
            }
        }
        if(chatCompletionResult != null) {
            ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource(getAgentName(), agent.getAgentConfig().getId());
            BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
            chatCompletionResult = chatCompletionResultWithSource;
            try {
                SkillMap skillMap = new SkillMap();
                IntentResponse intentResponse = skillMap.intentDetect(ChatCompletionUtil.getLastMessage(chatCompletionRequest));
                List<String> keywords = intentResponse.getKeywords();
                AgentIntentScore agentIntentScore = skillMap.agentIntentScore(agent.getAgentConfig().getId(), keywords);
                Double scoring = 0.0;
                if(agentIntentScore == null) {
                    scoring = skillMap.scoring(ChatCompletionUtil.getLastMessage(chatCompletionRequest), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
                    if(agent instanceof LlmProxyAgent) {
                        scoring = Math.max(scoring, 1.0);
                    }
                    skillMap.saveAgentScore(agent.getAgentConfig(), keywords, scoring);
                } else {
                    scoring = agentIntentScore.getScore();
                }
                calPriority = getPriority() + scoring;
            } catch (Exception e) {
                log.error("saveAgentScore error", e);
            }
        }
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
        return result;
    }

    private void feeAgentScoring(ChatCompletionRequest chatCompletionRequest, SkillMap skillMap, List<String> keywords) {
        executorService.submit(() -> {
            ChatCompletionResult chatCompletionResult;
            chatCompletionResult = agent.communicate(chatCompletionRequest);
            double score = skillMap.scoring(ChatCompletionUtil.getLastMessage(chatCompletionRequest), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            skillMap.saveAgentScore(agent.getAgentConfig(), keywords, score);
        });
    }

}
