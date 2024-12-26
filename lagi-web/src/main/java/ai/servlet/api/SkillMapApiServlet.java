package ai.servlet.api;

import ai.config.pojo.AgentConfig;
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
import java.util.Set;
import java.util.stream.Collectors;

public class SkillMapApiServlet extends RestfulServlet {

    private final SkillMap skillMap =  new SkillMap();
    private AgentService agentService;

    @Post("relatedAgents")
    public List<AgentIntentScore> queryRelatedAgents(@Body ChatCompletionRequest request) {
        IntentResponse intent = skillMap.intentDetect(request);
        if(intent == null) {
            return null;
        }
        try {
            LagiAgentListResponse lagiAgentList = agentService.getLagiAgentList(null, 1, 2000, "true");
            if(lagiAgentList == null) {
                return null;
            }
            List<AgentConfig> data = lagiAgentList.getData();
            if(data == null || data.isEmpty()) {
                return null;
            }
            Set<Integer> collect = data.stream().map(AgentConfig::getId).collect(Collectors.toSet());
            List<AgentIntentScore> agents = skillMap.getAgentIntentScoreByIntentKeyword(intent.getKeywords());
            return agents.stream().filter(agent -> collect.contains(agent.getAgentId())).collect(Collectors.toList());
        } catch (Exception ignored) {

        }
        return null;
    }


}
