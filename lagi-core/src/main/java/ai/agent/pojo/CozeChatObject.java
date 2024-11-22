package ai.agent.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeChatObject {
    private String id;
    private String conversation_id;
    private String bot_id;
    private Long completed_at;
    private CozeLastError last_error;
    private String status;
    private CozeUsage usage;
}
