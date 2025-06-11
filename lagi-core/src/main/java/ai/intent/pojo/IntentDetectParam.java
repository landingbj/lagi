package ai.intent.pojo;

import ai.agent.Agent;
import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.LLmRequest;
import lombok.Data;

import java.util.List;

@Data
public class IntentDetectParam {
    private LLmRequest llmRequest;
    private List<Agent<ChatCompletionRequest, ChatCompletionResult>> allAgents;
    private List<ILlmAdapter> userLlmAdapters;
}
