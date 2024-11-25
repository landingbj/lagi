package ai.embedding.impl;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.common.pojo.EmbeddingConfig;
import ai.embedding.EmbeddingConstant;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.embedding.pojo.OpenAIEmbeddingResponse;
import ai.embedding.pojo.TelecomEmbeddingRequest;
import ai.embedding.pojo.TelecomEmbeddingResponse;
import ai.utils.OkHttpUtil;
import com.google.common.cache.Cache;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TelecomGteEmbeddings implements Embeddings {
    private final Gson gson = new Gson();
    private static final Cache<List<String>, List<List<Float>>> cache = EmbeddingConstant.getEmbeddingCache();
    private final String apiEndpoint;

    public TelecomGteEmbeddings(EmbeddingConfig config) {
        apiEndpoint = config.getApi_endpoint();
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
      docs = docs.stream().filter(doc -> doc != null).collect(Collectors.toList());
        int batchSize = 5;
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
        } else {
            result = new ArrayList<>();
        }
        TelecomEmbeddingRequest request = TelecomEmbeddingRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .texts(docs).build();

        TelecomEmbeddingResponse embeddingResponse = null;

        try {
            String json = OkHttpUtil.post(apiEndpoint, gson.toJson(request));
            embeddingResponse = gson.fromJson(json, TelecomEmbeddingResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (embeddingResponse == null || embeddingResponse.getData() == null) {
            return Collections.emptyList();
        }
        result.addAll(embeddingResponse.getData().getChoices());
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
