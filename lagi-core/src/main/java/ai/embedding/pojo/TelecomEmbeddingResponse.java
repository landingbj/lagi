package ai.embedding.pojo;

import lombok.Data;

import java.util.List;

@Data
public class TelecomEmbeddingResponse {
    private Integer code;
    private String message;
    private EmbeddingData data;

    @Data
    public static class EmbeddingData {
        private List<List<Float>> choices;
    }
}
