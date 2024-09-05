package ai.llm.service;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;

public interface ChatCompletion {

    ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest);

    Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest);

}
