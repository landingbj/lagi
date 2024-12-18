package ai.agent.chat;

import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;

import static ai.utils.UnicodeStringUtil.decodeUnicode;
import static ai.utils.UnicodeStringUtil.replaceSlashUWithBackslashU;

public class BaseChatAgent extends AbstractChatAgent {

    public BaseChatAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.agentName = agentConfig.getName();
        this.badCase = agentConfig.getWrongCase() != null ? decodeUnicode(replaceSlashUWithBackslashU(agentConfig.getWrongCase())) : badCase;
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        return null;
    }
}
