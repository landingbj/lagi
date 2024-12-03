package ai.agent.chat.wenxin.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestMessage {
    private RequestMessageContent content;
}
