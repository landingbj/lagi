package ai.agent.pojo;

import lombok.Data;

@Data
public class StockResponse {
    private String code;
    private String message;
    private String requestId;
    private String date;
    private String answer;
    private String conversationId;
    private String messageId;
}
