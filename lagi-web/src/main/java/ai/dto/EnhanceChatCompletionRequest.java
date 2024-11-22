package ai.dto;

import ai.openai.pojo.ChatCompletionRequest;
import lombok.*;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class EnhanceChatCompletionRequest extends ChatCompletionRequest {
    private Boolean rag;
}
