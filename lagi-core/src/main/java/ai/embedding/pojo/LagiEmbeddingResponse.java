package ai.embedding.pojo;

import lombok.Data;

import java.util.List;

@Data
public class LagiEmbeddingResponse {
    private String status;
    private List<List<Float>> data;
}
