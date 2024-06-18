package ai.embedding.impl;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.embedding.EmbeddingConstant;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.embedding.pojo.OpenAIEmbeddingResponse;
import ai.common.pojo.EmbeddingConfig;
import com.google.common.cache.Cache;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandingEmbeddings implements Embeddings {
    private final Gson gson = new Gson();
    private static final AiServiceCall call = new AiServiceCall();
    private static final Cache<List<String>, List<List<Float>>> cache = EmbeddingConstant.getEmbeddingCache();

    public LandingEmbeddings(EmbeddingConfig config) {
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
        List<List<Float>> result = cache.getIfPresent(docs);
        if (result != null) {
            return result;
        }
        OpenAIEmbeddingRequest request = new OpenAIEmbeddingRequest();
        request.setInput(docs);
        Object[] params = {gson.toJson(request)};
        String json = call.callWS(AiServiceInfo.WSKngUrl, "embeddings", params)[0];
        OpenAIEmbeddingResponse embeddingResponse = gson.fromJson(json, OpenAIEmbeddingResponse.class);
        result = new ArrayList<>();
        for (OpenAIEmbeddingResponse.EmbeddingData data : embeddingResponse.getData()) {
            result.add(data.getEmbedding());
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
