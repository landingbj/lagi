package ai.agent.chat.wenxin.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestMessageContent {
    private String type;
    private RequestMessageValue value;
}
