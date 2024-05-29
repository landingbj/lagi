package ai.common.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class LLM {
    private EmbeddingConfig embedding;
    private List<Backend> backends;
    private String streamBackend;
    private List<Backend> chatBackends;
}