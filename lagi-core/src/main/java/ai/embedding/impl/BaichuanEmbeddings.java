package ai.embedding.impl;

import ai.common.client.AiServiceCall;
import ai.common.pojo.EmbeddingConfig;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.embedding.pojo.OpenAIEmbeddingResponse;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaichuanEmbeddings implements Embeddings {
    private final Gson gson = new Gson();
    private String openAIAPIKey;
    private String modelName;
    private static final String EMBEDDINGS_URL = "http://api.baichuan-ai.com/v1/embeddings";

    public BaichuanEmbeddings(EmbeddingConfig config) {
        this.openAIAPIKey = config.getApi_key();
        this.modelName = config.getModel_name();
    }

    public List<List<Float>> createEmbedding(List<String> docs) {
        List<List<Float>> result = new ArrayList<>();
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            OpenAIEmbeddingRequest openAIEmbeddingRequest = new OpenAIEmbeddingRequest();
            openAIEmbeddingRequest.setInput(docs);
            openAIEmbeddingRequest.setModel(this.modelName);
            String jsonBody = gson.toJson(openAIEmbeddingRequest);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(EMBEDDINGS_URL)
                    .addHeader("Authorization", "Bearer " + openAIAPIKey)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Unexpected code " + response);
                }
                OpenAIEmbeddingResponse openAIEmbeddingResponse = gson.fromJson(response.body().string(), OpenAIEmbeddingResponse.class);
                for (OpenAIEmbeddingResponse.EmbeddingData data : openAIEmbeddingResponse.getData()) {
                    result.add(data.getEmbedding());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Float> createEmbedding(String doc) {
        List<String> docs = new ArrayList<>();
        docs.add(doc);
        List<List<Float>> result = createEmbedding(docs);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }
}
