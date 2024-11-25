package ai.agent.pojo;


import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
public class CozeChatResultData {
    private String bot_id;
    private String content;
    private String content_type;
    private String conversation_id;
    private String id;
    private String role;
    private String type;
}
