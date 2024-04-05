package ai.embedding
        ;

import java.util.List;

public interface Embeddings {
    List<List<Float>> createEmbedding(List<String> docs);

    List<Float> createEmbedding(String doc);
}
