package ai.embedding;

import ai.embedding.impl.*;
import ai.common.pojo.EmbeddingConfig;

public class EmbeddingFactory {
    public static Embeddings getEmbedding(EmbeddingConfig config) {
        String type = config.getType();
        if (EmbeddingConstant.EMBEDDING_TYPE_OPENAI.equalsIgnoreCase(type)) {
            return new OpenAIEmbeddings(config);
        } else if (EmbeddingConstant.EMBEDDING_TYPE_ERNIE.equalsIgnoreCase(type)) {
            return new ErnieEmbeddings(config);
        } else if (EmbeddingConstant.EMBEDDING_TYPE_QWEN.equalsIgnoreCase(type)) {
            return new QwenEmbeddings(config);
        } else if (EmbeddingConstant.EMBEDDING_TYPE_LANDING.equalsIgnoreCase(type)) {
            return new LandingEmbeddings(config);
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }
}