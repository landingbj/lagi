package ai.llm.pojo;

import ai.openai.pojo.ChatCompletionResult;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class ChatCompletionResultWithSource extends ChatCompletionResult {
    private String source;
}
