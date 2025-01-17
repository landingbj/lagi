package ai.router.pojo;

import ai.openai.pojo.ChatCompletionResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteCompletionResult {
    private ChatCompletionResult result;
    private Double priority;
}
