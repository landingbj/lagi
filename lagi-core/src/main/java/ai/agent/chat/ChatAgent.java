package ai.agent.chat;

import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.Getter;

import java.io.IOException;

@Getter
abstract public class ChatAgent {
    protected AgentConfig agentConfig;
    abstract public ChatCompletionResult chat(ChatCompletionRequest request) throws IOException;
}
