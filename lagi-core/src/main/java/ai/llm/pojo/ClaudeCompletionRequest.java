package ai.llm.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClaudeCompletionRequest {
    private String model;
    private Integer max_tokens;
    private Double temperature;
    private Boolean stream;
    private List<Message> messages;
    private String system;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
