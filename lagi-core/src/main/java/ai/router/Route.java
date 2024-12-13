package ai.router;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class Route {
    private String rule;
    private Function<ChatCompletionRequest, ChatCompletionResult> handler;

    public ChatCompletionResult handle(ChatCompletionRequest request) {
        return handler.apply(request);
    }
}
