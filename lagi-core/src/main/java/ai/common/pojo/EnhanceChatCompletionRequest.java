package ai.common.pojo;

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

    private String userId;

    private String identity; // "leader" or "personnel"

    private Boolean meeting;

    private String business;// "HYJY" or "GSSW"
}
