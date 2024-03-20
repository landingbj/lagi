package ai.embedding;

import ai.embedding.impl.OpenAIEmbeddings;
import ai.embedding.impl.RandomEmbeddings;
import ai.migrate.pojo.EmbeddingConfig;

public class EmbeddingFactory {
    public static Embeddings getEmbedding(EmbeddingConfig config) {
        String type = config.getType();
        if (EmbeddingConstant.EMBEDDING_TYPE_OPENAI.equalsIgnoreCase(type)) {
            return new OpenAIEmbeddings(config);
        } else if (EmbeddingConstant.EMBEDDING_TYPE_RANDOM.equalsIgnoreCase(type)) {
            return new RandomEmbeddings(config);
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }
}