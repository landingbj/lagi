package ai.llm.pojo;

import ai.openai.pojo.ChatCompletionRequest;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EnhanceChatCompletionRequest extends ChatCompletionRequest {
    private String userId;
    private String ip;
    private String browserIp;
    private Integer priority;
    private Boolean rag;
}
