package ai.embedding.impl;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.embedding.pojo.OpenAIEmbeddingResponse;
import ai.common.pojo.EmbeddingConfig;
import ai.learning.pojo.QaPairResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandingEmbeddings implements Embeddings {
    private final Gson gson = new Gson();
    private static final AiServiceCall call = new AiServiceCall();

    public LandingEmbeddings(EmbeddingConfig config) {
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
        OpenAIEmbeddingRequest request = new OpenAIEmbeddingRequest();
        request.setInput(docs);
        Object[] params = {gson.toJson(request)};
        String json = call.callWS(AiServiceInfo.WSKngUrl, "embeddings", params)[0];
        OpenAIEmbeddingResponse embeddingResponse = gson.fromJson(json, OpenAIEmbeddingResponse.class);
        List<List<Float>> result = new ArrayList<>();
        for (OpenAIEmbeddingResponse.EmbeddingData data : embeddingResponse.getData()) {
            result.add(data.getEmbedding());
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
