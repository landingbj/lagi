package ai.agent.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockRequest {
    private String app_id;
    private String query;
    private String conversation_id;
    private boolean stream;
}
