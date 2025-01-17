package ai.llm.pojo;

import lombok.Data;

@Data
public class ClaudeStreamResponse {
    private String type;
    private Message message;
    private int index;
    private ContentBlock content_block;
    private Delta delta;
    private Usage usage;

    @Data
    public static class Message {
        private String id;
        private String model;
        private String type;
        private String role;
        private Usage usage;
    }

    @Data
    public static class ContentBlock {
        private String type;
        private String text;
    }

    @Data
    public static class Delta {
        private String type;
        private String text;
        private String stop_reason;
    }

    @Data
    public static class Usage {
        private int input_tokens;
        private int output_tokens;
    }
}
