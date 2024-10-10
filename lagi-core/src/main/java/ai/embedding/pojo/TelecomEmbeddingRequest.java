package ai.embedding.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TelecomEmbeddingRequest {
    private String requestId;
    private List<String> texts;
}
