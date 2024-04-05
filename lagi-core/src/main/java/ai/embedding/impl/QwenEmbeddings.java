package ai.embedding.impl;

import ai.embedding.Embeddings;
import ai.common.pojo.EmbeddingConfig;
import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QwenEmbeddings implements Embeddings {
    private String apiKey;

    public QwenEmbeddings(EmbeddingConfig config) {
        this.apiKey = config.getApi_key();
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
        TextEmbeddingParam param = TextEmbeddingParam
                .builder()
                .apiKey(this.apiKey)
                .model(TextEmbedding.Models.TEXT_EMBEDDING_V2)
                .texts(docs).build();
        TextEmbedding textEmbedding = new TextEmbedding();
        List<List<Float>> result = new ArrayList<>();

        try {
            TextEmbeddingResult textEmbeddingResult = textEmbedding.call(param);
            for (TextEmbeddingResultItem item : textEmbeddingResult.getOutput().getEmbeddings()) {
                List<Float> embedding = new ArrayList<>();
                for (Double value : item.getEmbedding()) {
                    embedding.add(value.floatValue());
                }
                result.add(embedding);
            }
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<Float> createEmbedding(String doc) {
        List<String> docs = Collections.singletonList(doc);
        List<List<Float>> result = createEmbedding(docs);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
}
