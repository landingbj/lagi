package ai.router.pojo;

import ai.openai.pojo.ChatCompletionResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteAgentResult {
    private List<ChatCompletionResult> result;
    private Double priority;
}
