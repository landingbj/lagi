package ai.router.pojo;

import ai.intent.pojo.IntentRouteResult;
import ai.openai.pojo.ChatCompletionRequest;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class LLmRequest extends ChatCompletionRequest {
//    private String agentId;
    private Integer agentId;
    private String worker;
    private String userId;
    private Boolean think;
    private IntentRouteResult intent;
}
