package ai.agent.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XiaoxinRequest {
    private String queryText;
    private String sessionId;
}
