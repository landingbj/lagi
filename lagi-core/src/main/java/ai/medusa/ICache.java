package ai.medusa;

import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionResult;

import java.util.Map;

public interface ICache {
    ChatCompletionResult get(PromptInput promptInput);

    void put(PromptInput promptInput, ChatCompletionResult chatCompletionResult);

    int size();

    ChatCompletionResult locate(PromptInput promptInput);

    PromptPool getPromptPool();

    void startProcessingPrompt();
}
