package ai.agent.pojo;

import lombok.Data;

@Data
public class NewConversationResponse {
    private String code;
    private String message;
    private String request_id;
    private String conversation_id;
}


