package ai.intent.pojo;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.Data;

import java.util.List;

@Data
public class IntentDetectResult {
    private IntentResult modal;
    private List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents;
}
