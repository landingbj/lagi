package ai.lagi.adapter;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;

public interface ILlmAdapter {
    ChatCompletionResult completions(ChatCompletionRequest request);

    Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest);
}
