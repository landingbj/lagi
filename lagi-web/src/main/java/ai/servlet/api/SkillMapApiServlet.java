package ai.servlet.api;

import ai.openai.pojo.ChatCompletionRequest;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Post;
import ai.worker.SkillMap;
import ai.worker.pojo.AgentIntentScore;
import ai.worker.pojo.IntentResponse;

import java.util.List;

public class SkillMapApiServlet extends RestfulServlet {

    private SkillMap skillMap =  new SkillMap();

    @Post("relatedAgents")
    public List<AgentIntentScore> queryRelatedAgents(@Body ChatCompletionRequest request) {
        IntentResponse intent = skillMap.intentDetect(request);
        if(intent == null) {
            return null;
        }
        return skillMap.getAgentIntentScoreByIntentKeyword(intent.getKeywords());
    }


}
