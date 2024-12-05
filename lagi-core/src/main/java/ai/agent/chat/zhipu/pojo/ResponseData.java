package ai.agent.chat.zhipu.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseData {
    private String conversation_id;
    private String id;
}
