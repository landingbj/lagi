package ai.lagi.adapter;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;

public interface ILlmAdapter {
    ChatCompletionResult completions(ChatCompletionRequest request);
}
