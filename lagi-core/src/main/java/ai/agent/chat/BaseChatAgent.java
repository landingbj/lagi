package ai.agent.chat;

import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.UnicodeStringUtil;

import static ai.utils.UnicodeStringUtil.decodeUnicode;
import static ai.utils.UnicodeStringUtil.replaceSlashUWithBackslashU;

public class BaseChatAgent extends AbstractChatAgent{

    public BaseChatAgent(AgentConfig agentConfig) {
        agentConfig.setWrongCase(agentConfig.getWrongCase() != null ? decodeUnicode(replaceSlashUWithBackslashU(agentConfig.getWrongCase())) : "抱歉");
        this.agentConfig = agentConfig;
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        return null;
    }
}
