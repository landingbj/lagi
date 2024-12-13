package ai.router.pojo;

import ai.openai.pojo.ChatCompletionRequest;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LLmRequest extends ChatCompletionRequest {
    private String agentId;
    private String router;
    private String userInfo;
}
