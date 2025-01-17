package ai.router;


import ai.agent.Agent;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.learn.questionAnswer.KShingle;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.llm.service.FreezingService;
import ai.llm.utils.LLMErrorConstants;
import ai.medusa.utils.LCS;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import ai.router.pojo.RouteCompletionResult;
import ai.router.utils.RouteGlobal;
import ai.utils.SensitiveWordUtil;
import ai.utils.WorkPriorityWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.SkillMap;
import ai.worker.WorkerGlobal;
import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.util.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class BasicRoute extends Route {
    public BasicRoute(String name) {
        super(name);
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
        log.info("{} .myMapping: add = {}", getAgentName(agent), add);
        log.info("{} .myMapping: positive = {}", getAgentName(agent), positive);
        log.info("{} .myMapping: negative = {}", getAgentName(agent), negative);
        log.info("{} .myMapping: calPriority = {}", getAgentName(agent), calcPriority);
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
        Agent<ChatCompletionRequest, ChatCompletionResult> agent = getAgentHandler(name);
        ChatCompletionResult chatCompletionResult;
        double calPriority = 0;
        chatCompletionResult = agent.communicate(request);
        if (request != null) {
            ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource(getAgentName(agent));
            BeanUtil.copyProperties(chatCompletionResult, chatCompletionResultWithSource);
            chatCompletionResult = chatCompletionResultWithSource;
            calPriority = calculatePriority(request, chatCompletionResult, agent);
            try {
                SkillMap skillMap = new SkillMap();
                skillMap.saveAgentScore(agent.getAgentConfig(), ChatCompletionUtil.getLastMessage(request), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            } catch (Exception e) {
                log.error("saveAgentScore error", e);
            }
        }
        if (chatCompletionResult != null) {
            return RouteAgentResult.builder().result(Collections.singletonList(chatCompletionResult)).priority(calPriority).build();
        }
        return null;
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
