package ai.agent.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewConversationRequest {
    private String app_id;
}


