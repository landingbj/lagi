package ai.embedding.impl;

import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingResponse;
import ai.migrate.pojo.EmbeddingConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LandingEmbeddings implements Embeddings {
    private Gson gson = new Gson();
    private String apiEndpoint;

    public LandingEmbeddings(EmbeddingConfig config) {
        this.apiEndpoint = config.getApi_endpoint();
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("input", gson.toJsonTree(docs));
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(gson.toJson(jsonObject), JSON);
        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(body)
                .build();
        List<List<Float>> result = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            OpenAIEmbeddingResponse embeddingResponse = gson.fromJson(response.body().string(), OpenAIEmbeddingResponse.class);
            for (OpenAIEmbeddingResponse.EmbeddingData data : embeddingResponse.getData()) {
                result.add(data.getEmbedding());
            }
        } catch (IOException e) {
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
