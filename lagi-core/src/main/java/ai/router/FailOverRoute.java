package ai.router;


import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.util.Lists;

import java.util.List;

@Slf4j
public class FailOverRoute extends Route{
    public FailOverRoute(String name) {
        super(name);
    }

    public List<ChatCompletionResult> invoke(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        for (Agent<ChatCompletionRequest, ChatCompletionResult> agent : agents) {
            try {
                return Lists.newArrayList(agent.communicate(request));
            } catch (Exception e) {
                log.error("polling error", e);
            }
        }
        return null;
    }
}
