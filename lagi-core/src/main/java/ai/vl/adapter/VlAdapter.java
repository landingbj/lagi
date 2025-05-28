package ai.vl.adapter;

import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.VlChatCompletionRequest;
import io.reactivex.Observable;

public interface VlAdapter {
    ChatCompletionResult completions(VlChatCompletionRequest request);

    Observable<ChatCompletionResult> streamCompletions(VlChatCompletionRequest chatCompletionRequest);
}
