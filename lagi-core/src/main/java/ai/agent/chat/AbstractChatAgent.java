package ai.agent.chat;

import ai.agent.Agent;
import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.Getter;

@Getter
abstract public class AbstractChatAgent extends Agent<ChatCompletionRequest, ChatCompletionResult> {
    protected AgentConfig agentConfig;

    @Override
    public void connect() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public void send(ChatCompletionRequest request) {
    }

    @Override
    public ChatCompletionResult receive() {
        return null;
    }


}
