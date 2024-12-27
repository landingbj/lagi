package ai.servlet.api;

import ai.config.pojo.AgentConfig;
import ai.dto.FeeRequiredAgentRequest;
import ai.migrate.service.AgentService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Post;
import ai.servlet.dto.LagiAgentListResponse;
import ai.worker.skillMap.SkillMap;
import ai.worker.pojo.AgentIntentScore;
import ai.worker.pojo.IntentResponse;

import java.util.List;
import java.util.stream.Collectors;

public class SkillMapApiServlet extends RestfulServlet {

    private final SkillMap skillMap =  new SkillMap();
    private AgentService agentService = new AgentService();

    @Post("relatedAgents")
    public List<AgentConfig> queryRelatedAgents(@Body ChatCompletionRequest request) {
        IntentResponse intent = skillMap.intentDetect(request);
        if(intent == null) {
            return null;
        }
        try {
            List<AgentIntentScore> agentIntentScoreByIntentKeyword = skillMap.getAgentIntentScoreByIntentKeyword(intent.getKeywords());
            List<Integer> collect = agentIntentScoreByIntentKeyword.stream().map(AgentIntentScore::getAgentId).collect(Collectors.toList());
            if(collect.isEmpty()) {
                return null;
            }
            FeeRequiredAgentRequest feeRequiredAgentRequest = new FeeRequiredAgentRequest();
            feeRequiredAgentRequest.setAgentIds(collect);
            feeRequiredAgentRequest.setIsFeeRequired(true);
            LagiAgentListResponse feeRequiredAgent = agentService.getFeeRequiredAgent(feeRequiredAgentRequest);
            List<AgentConfig> data = feeRequiredAgent.getData();
            data.forEach(agentConfig -> {
                agentConfig.setAppId(null);
                agentConfig.setToken(null);
                agentConfig.setApiKey(null);
                agentConfig.setDriver(null);
            });
            return data;
        } catch (Exception ignored) {
        }
        return null;
    }


}
