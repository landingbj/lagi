package ai.embedding.impl;

import ai.embedding.EmbeddingConstant;
import ai.embedding.Embeddings;
import ai.common.pojo.EmbeddingConfig;
import com.alibaba.dashscope.embeddings.*;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.common.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QwenEmbeddings implements Embeddings {
    private final String apiKey;
    private static final Cache<List<String>, List<List<Float>>> cache = EmbeddingConstant.getEmbeddingCache();

    public QwenEmbeddings(EmbeddingConfig config) {
        this.apiKey = config.getApi_key();
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
        int batchSize = 25;
        List<List<Float>> result = new ArrayList<>();
        for (int i = 0; i < docs.size(); i += batchSize) {
            List<String> batchDocs = docs.subList(i, Math.min(i + batchSize, docs.size()));
            List<List<Float>> batchResult = createEmbeddingBatch(batchDocs);
            result.addAll(batchResult);
        }
        return result;
    }

    public List<List<Float>> createEmbeddingBatch(List<String> docs) {
        List<List<Float>> result = cache.getIfPresent(docs);
        if (result != null) {
            return result;
        }
        TextEmbeddingParam param = TextEmbeddingParam
                .builder()
                .apiKey(this.apiKey)
                .model(TextEmbedding.Models.TEXT_EMBEDDING_V2)
                .texts(docs).build();
        TextEmbedding textEmbedding = new TextEmbedding();
        result = new ArrayList<>();
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
        if (!result.isEmpty()) {
            cache.put(docs, result);
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
