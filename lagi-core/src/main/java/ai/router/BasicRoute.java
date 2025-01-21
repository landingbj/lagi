package ai.router;


import ai.agent.Agent;
import ai.agent.proxy.LlmProxyAgent;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.utils.ThreadPoolManager;
import ai.learn.questionAnswer.KShingle;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.llm.service.FreezingService;
import ai.medusa.utils.LCS;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import ai.router.pojo.RouteCompletionResult;
import ai.utils.SensitiveWordUtil;
import ai.utils.WorkPriorityWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.WorkerGlobal;
import ai.worker.pojo.AgentIntentScore;
import ai.worker.pojo.IntentResponse;
import ai.worker.skillMap.SkillMap;
import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Slf4j
public class BasicRoute extends Route {

    private static final ExecutorService executorService;
    static {
        ThreadPoolManager.registerExecutor("feeScoring");
        executorService = ThreadPoolManager.getExecutor("feeScoring");
    }

    public BasicRoute(String name) {
        super(name);
    }

    public BasicRoute(Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        super(agent);
    }

    public String getAgentName(Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        return agent.getAgentConfig().getName();
    }

    public String getBadCase(Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        return agent.getAgentConfig().getWrongCase() == null ? "抱歉" : agent.getAgentConfig().getWrongCase();
    }

    public double getSimilarity(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult, Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        KShingle kShingle = new KShingle();
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        double[] similarity = kShingle.similarity(question, answer, 2);
        return similarity[0];
    }

    public double getBadCaseSimilarity(String badCase, ChatCompletionResult chatCompletionResult, Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        Set<String> longestCommonSubstrings = LCS.findLongestCommonSubstrings(badCase, answer, 2);
        return LCS.getLcsRatio(badCase, longestCommonSubstrings);
    }

    public double calculatePriority(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult, Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        double positive = getSimilarity(chatCompletionRequest, chatCompletionResult, agent);
        double negative = getBadCaseSimilarity(getBadCase(agent), chatCompletionResult, agent);
        double add = getPriorityWordPriority(chatCompletionRequest, chatCompletionResult, agent);
        double calcPriority;
        if (negative > 0.8) {
            calcPriority = WorkerGlobal.MAPPER_PRIORITY + (negative * -10);
        } else {
            calcPriority = positive * 10 + WorkerGlobal.MAPPER_PRIORITY + add;
        }
        log.info("{} - {} .myMapping: add = {}", getAgentName(agent) , agent.getAgentConfig().getId(), add);
        log.info("{} - {} .myMapping: positive = {}", getAgentName(agent), agent.getAgentConfig().getId(), positive);
        log.info("{} - {} .myMapping: negative = {}", getAgentName(agent), agent.getAgentConfig().getId(), negative);
        log.info("{} - {} .myMapping: calPriority = {}", getAgentName(agent), agent.getAgentConfig().getId(), calcPriority);
        return calcPriority;
    }

    public double getPriorityWordPriority(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult, Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        boolean work = WorkPriorityWordUtil.isPriorityWord(getAgentName(agent), question, answer);
        if (work) return 10;
        else return 0;
    }

    @Override
    public RouteAgentResult invokeAgent(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        Agent<ChatCompletionRequest, ChatCompletionResult> agent = getAgent();
        ChatCompletionResult chatCompletionResult = null;
        double calPriority = 0;
        boolean isFeeRequired = Boolean.TRUE.equals(agent.getAgentConfig().getIsFeeRequired());
        boolean canOutPut = Boolean.TRUE.equals(agent.getAgentConfig().getCanOutPut());
        if ((!isFeeRequired) || canOutPut) {
            // free or paid  agent  can out put
            chatCompletionResult = agent.communicate(request);
            if (chatCompletionResult != null) {
                ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource(agent.getAgentConfig().getName(), agent.getAgentConfig().getId());
                BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
                chatCompletionResult = chatCompletionResultWithSource;
                try {
                    if (!(agent instanceof LlmProxyAgent)) {
                        Double scoring = getOrInsertScore(request, agent, chatCompletionResult);
                        calPriority = calculatePriority(request, chatCompletionResult, agent) + scoring;
                    } else {
                        calPriority = calculatePriority(request, chatCompletionResult, agent);
                    }
                } catch (Exception e) {
                    log.error("saveAgentScore error", e);
                }
                return RouteAgentResult.builder().result(Collections.singletonList(chatCompletionResult)).priority(calPriority).build();
            }
        } else {
            // not free  detect
            SkillMap skillMap = new SkillMap();
            IntentResponse intentResponse = skillMap.intentDetect(ChatCompletionUtil.getLastMessage(request));
            List<String> keywords = intentResponse.getKeywords();
            AgentIntentScore agentIntentScore = skillMap.agentIntentScore(agent.getAgentConfig().getId(), keywords);
            // if not scoring
            if (agentIntentScore == null) {
                feeAgentScoring(agent, request, skillMap, keywords);
            }
        }

        return null;
    }

    private Double getOrInsertScore(ChatCompletionRequest request, Agent<ChatCompletionRequest, ChatCompletionResult> agent, ChatCompletionResult chatCompletionResult) {
        SkillMap skillMap = new SkillMap();
        IntentResponse intentResponse = skillMap.intentDetect(ChatCompletionUtil.getLastMessage(request));
        List<String> keywords = intentResponse.getKeywords();
        AgentIntentScore agentIntentScore = skillMap.agentIntentScore(agent.getAgentConfig().getId(), keywords);
        Double scoring = 0.0;
        if (agentIntentScore == null) {
            scoring = skillMap.scoring(ChatCompletionUtil.getLastMessage(request), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            skillMap.saveAgentScore(agent.getAgentConfig(), keywords, scoring);
        } else {
            scoring = agentIntentScore.getScore();
        }
        return scoring;
    }

    private void feeAgentScoring(Agent<ChatCompletionRequest, ChatCompletionResult> agent, ChatCompletionRequest chatCompletionRequest, SkillMap skillMap, List<String> keywords) {
        executorService.submit(() -> {
            ChatCompletionResult chatCompletionResult;
            chatCompletionResult = agent.communicate(chatCompletionRequest);
            double score = skillMap.scoring(ChatCompletionUtil.getLastMessage(chatCompletionRequest), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            skillMap.saveAgentScore(agent.getAgentConfig(), keywords, score);
        });
    }

    @Override
    public RouteCompletionResult invokeLlm(ChatCompletionRequest request) {
        ILlmAdapter adapter = llmAdapters.get(name);
        if (adapter != null && FreezingService.notFreezingAdapter(adapter)) {
            ChatCompletionRequest copy = new ChatCompletionRequest();
            BeanUtil.copyProperties(request, copy);
            try {
                ChatCompletionResult result = SensitiveWordUtil.filter(adapter.completions(copy));
                FreezingService.unfreezeAdapter(adapter);
                double priority = ((ModelService) adapter).getPriority();
                return RouteCompletionResult.builder().result(result).priority(priority).build();
            } catch (RRException e) {
                FreezingService.freezingAdapterByErrorCode(adapter, e.getCode());
            }
        }
        return null;
    }
}
