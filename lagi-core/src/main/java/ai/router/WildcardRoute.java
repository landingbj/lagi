package ai.router;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WildcardRoute extends Route {
    public WildcardRoute(String name) {
        super(name);
    }

    @Override
    public List<ChatCompletionResult> invoke(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        if (agents == null || agents.size() != 1) {
            return null;
        }
        return Lists.newArrayList(agents.get(0).communicate(request));
    }
}
