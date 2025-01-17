package ai.llm.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ClaudeCompletionResponse {
    private List<Content> content;
    private String id;
    private String model;
    private String role;
    private String stop_reason;
    private String stop_sequence;
    private String type;
    private Usage usage;

    @Data
    public static class Content {
        private String text;
        private String type;
    }

    @Data
    public static class Usage {
        private int input_tokens;
        private int output_tokens;
    }
}
