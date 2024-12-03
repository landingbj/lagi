package ai.agent.chat.wenxin.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerRequest {

    private String threadId;
    private RequestMessage message;
    private String source;
    private String from;
    private String openId;
}
