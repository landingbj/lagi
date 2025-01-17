package ai.router;

import ai.agent.Agent;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.RouteAgentResult;
import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WildcardRoute extends Route {
    public WildcardRoute(String name) {
        super(name);
    }

    @Override
    public RouteAgentResult invokeAgent(ChatCompletionRequest request, List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents) {
        if (agents == null || agents.size() != 1) {
            return null;
        }
        ChatCompletionResult communicate = agents.get(0).communicate(request);
        if(communicate != null) {
            ChatCompletionResultWithSource chatCompletionResultWithSource = new ChatCompletionResultWithSource();
            BeanUtil.copyProperties(communicate, chatCompletionResultWithSource);
            chatCompletionResultWithSource.setSource(agents.get(0).getAgentConfig().getName());
            communicate = chatCompletionResultWithSource;
        }
        return RouteAgentResult.builder().result(Lists.newArrayList(communicate)).build();
    }
}
