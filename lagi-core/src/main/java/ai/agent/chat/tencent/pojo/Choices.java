package ai.agent.chat.tencent.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Choices {
    private Integer index;
    private String finish_reason;
    private Message message;
}
