package ai.llm.pojo;

import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LlmApiResponse {
    private Integer code;
    private String msg;
    private ChatCompletionResult data;
    private Observable<ChatCompletionResult> streamData;
}
