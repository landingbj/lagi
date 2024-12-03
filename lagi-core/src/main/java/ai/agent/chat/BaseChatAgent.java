package ai.agent.chat;

import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;

public class BaseChatAgent extends AbstractChatAgent{

    public BaseChatAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.agentName = agentConfig.getName();
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        return null;
    }
}
