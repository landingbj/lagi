package ai.worker.skillMap;

import ai.agent.Agent;
import ai.agent.chat.rag.LocalRagAgent;
import ai.common.pojo.Backend;
import ai.common.utils.ThreadPoolManager;
import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.pojo.AgentIntentScore;
import ai.worker.pojo.IntentResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class SkillMapUtil {
    private static final SkillMap skillMap = new SkillMap();
    private static final Double THRESHOLD = 5.0;

    private static final String RAG_AGENT = "ai.agent.chat.rag.LocalRagAgent";
    private static final ExecutorService executorService;
    private static final List<Agent<ChatCompletionRequest, ChatCompletionResult>> llmAndAgentList;
    private static Agent<ChatCompletionRequest, ChatCompletionResult> highestPriorityLlm;

    static {
        llmAndAgentList = initAgents();
        ThreadPoolManager.registerExecutor("SkillMapUtil");
        executorService= ThreadPoolManager.getExecutor("SkillMapUtil");
    }

    public static List<Agent<ChatCompletionRequest, ChatCompletionResult>> initAgents() {
        Map<String, Integer> ragAgentMap = skillMap.getRagAgentIdName();
        List<AgentConfig> llmAndAgentConfigList = new ArrayList<>();
        List<AgentConfig> agentConfigList = LagiGlobal.getConfig().getAgents();
        llmAndAgentConfigList.addAll(agentConfigList);
        String highestPriorityModel = null;
        int highestPriority = 0;
        for (Backend llmConfig: LagiGlobal.getConfig().getLLM().getChatBackends()) {
            if (llmConfig.getPriority() > highestPriority) {
                highestPriority = llmConfig.getPriority();
                highestPriorityModel = llmConfig.getDefaultModel();
            }
            AgentConfig agentConfig = new AgentConfig();
            agentConfig.setDriver(RAG_AGENT);
            agentConfig.setName(llmConfig.getDefaultModel());
            llmAndAgentConfigList.add(agentConfig);
        }
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> llmAndAgentList = convert2AgentList(llmAndAgentConfigList, new HashMap<>());

        for (Agent<ChatCompletionRequest, ChatCompletionResult> agent : llmAndAgentList) {
            agent.getAgentConfig().setIsFeeRequired(false);
            agent.getAgentConfig().setCanOutPut(true);
            if (agent instanceof LocalRagAgent) {
                LocalRagAgent ragAgent = (LocalRagAgent) agent;
                Integer agentId = ragAgentMap.get(ragAgent.getAgentConfig().getName());
                if (agentId != null) {
                    ragAgent.getAgentConfig().setId(agentId);
                } else {
                    ragAgent.getAgentConfig().setId(skillMap.getRandomRagAgentId());
                }
                if (ragAgent.getAgentConfig().getName().equalsIgnoreCase(highestPriorityModel)) {
                    highestPriorityLlm = ragAgent;
                }
            }
        }
        return llmAndAgentList;
    }

    public static Agent<ChatCompletionRequest, ChatCompletionResult> getHighestPriorityLlm() {
        return highestPriorityLlm;
    }

    public static List<Agent<ChatCompletionRequest, ChatCompletionResult>> getLlmAndAgentList() {
        return llmAndAgentList;
    }

    public static List<Agent<ChatCompletionRequest, ChatCompletionResult>> rankAgentByIntentKeyword(
            List<Agent<ChatCompletionRequest, ChatCompletionResult>> agentList,
            String question) {
        return rankAgentByIntentKeyword(agentList, question, THRESHOLD);
    }

    public static List<Agent<ChatCompletionRequest, ChatCompletionResult>> rankAgentByIntentKeyword(
            List<Agent<ChatCompletionRequest, ChatCompletionResult>> agentList,
            String question, Double edge) {
        return skillMap.rankAgentByIntentKeyword(agentList, question, edge);
    }

    public static List<Agent<ChatCompletionRequest, ChatCompletionResult>> convert2AgentList(List<AgentConfig> agentConfigs, Map<Integer, Boolean> haveABalance) {
        Map<String, Constructor<?>> agentMap = new HashMap<>();
        return agentConfigs.stream().map(agentConfig -> {
            if (agentConfig == null) {
                return null;
            }
            String driver = agentConfig.getDriver();
            Agent<ChatCompletionRequest, ChatCompletionResult> agent = null;
            agentConfig.setCanOutPut(haveABalance.getOrDefault(agentConfig.getId(), false));
            if (!agentMap.containsKey(driver)) {
                try {
                    Class<?> aClass = Class.forName(driver);
                    Constructor<?> constructor = aClass.getConstructor(AgentConfig.class);
                    agentMap.put(driver, constructor);
                    agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) constructor.newInstance(agentConfig);
                } catch (Exception ignored) {
                }
            } else {
                Constructor<?> constructor = agentMap.get(driver);
                try {
                    agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) constructor.newInstance(agentConfig);
                } catch (Exception ignored) {
                }
            }
            return agent;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static void scoreAgents(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agentList) {
        executorService.submit(() -> {
            request.setStream(false);
            IntentResponse intentResponse = skillMap.intentDetect(ChatCompletionUtil.getLastMessage(request));
            for (Agent<ChatCompletionRequest, ChatCompletionResult> agent : agentList) {
                if (agent instanceof LocalRagAgent) {
                    request.setModel(agent.getAgentConfig().getName());
                }
                try {
                    saveScore(intentResponse, request, agent);
                } catch (Exception e) {
                    log.error("scoreAgents error", e);
                }
            }
        });
    }

    public static double getOrInsertScore(ChatCompletionRequest request, Agent<ChatCompletionRequest, ChatCompletionResult> agent, ChatCompletionResult chatCompletionResult) {
        IntentResponse intentResponse = skillMap.intentDetect(ChatCompletionUtil.getLastMessage(request));
        return getOrInsertScore(intentResponse, request, agent, chatCompletionResult);
    }

    public static double getOrInsertScore(IntentResponse intentResponse, ChatCompletionRequest request, Agent<ChatCompletionRequest, ChatCompletionResult> agent, ChatCompletionResult chatCompletionResult) {
        List<String> keywords = intentResponse.getKeywords();
        AgentIntentScore agentIntentScore = skillMap.agentIntentScore(agent.getAgentConfig().getId(), keywords);
        double scoring;
        if (agentIntentScore == null) {
            scoring = skillMap.scoring(ChatCompletionUtil.getLastMessage(request), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            skillMap.saveAgentScore(agent.getAgentConfig(), keywords, scoring);
        } else {
            scoring = agentIntentScore.getScore();
        }
        return scoring;
    }

    public static void saveScore(IntentResponse intentResponse, ChatCompletionRequest request, Agent<ChatCompletionRequest, ChatCompletionResult> agent) {
        List<String> keywords = intentResponse.getKeywords();
        AgentIntentScore agentIntentScore = skillMap.agentIntentScore(agent.getAgentConfig().getId(), keywords);
        if (agentIntentScore == null) {
            ChatCompletionResult chatCompletionResult = agent.communicate(request);
            double scoring = skillMap.scoring(ChatCompletionUtil.getLastMessage(request), ChatCompletionUtil.getFirstAnswer(chatCompletionResult));
            skillMap.saveAgentScore(agent.getAgentConfig(), keywords, scoring);
        }
    }
}
