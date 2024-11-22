package ai.agent.citic;

import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.Getter;

import java.io.IOException;

abstract public class CiticAgent {
    @Getter
    protected AgentConfig agentConfig;
    abstract public ChatCompletionResult chat(ChatCompletionRequest request) throws IOException;
}
