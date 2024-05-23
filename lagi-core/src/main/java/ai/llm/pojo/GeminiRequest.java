package ai.llm.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class GeminiRequest {
    private List<Content> contents;
    private GenerationConfig generationConfig;
    private SystemInstruction systemInstruction;

    @Data
    @Builder
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Data
    @Builder
    public static class Part {
        private String text;
    }

    @Data
    public static class GenerationConfig {
        private int maxOutputTokens;
        private double temperature;
    }

    @Data
    public static class SystemInstruction {
        private Part parts;
    }
}

