package ai.agent.pojo;

import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeChatRequest {
    private String bot_id;
    private String user_id;
    private boolean stream;
    private boolean auto_save_history;
    private List<CozeAdditionalMessages> additional_messages;
}
