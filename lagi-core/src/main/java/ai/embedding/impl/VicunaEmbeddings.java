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
import java.util.concurrent.TimeUnit;

public class VicunaEmbeddings implements Embeddings {
    private final Gson gson = new Gson();
    private static final AiServiceCall call = new AiServiceCall();
    private String openAIAPIKey;
    private String modelName;
    private String apiEndpoint;
    private ConnectionPool connectionPool = new ConnectionPool(
            100, // 最大空闲连接数
            100, // 保持连接的时间
            TimeUnit.MINUTES
    );

    public VicunaEmbeddings(EmbeddingConfig config) {
        this.openAIAPIKey = config.getApi_key();
        this.modelName = config.getModel_name();
        this.apiEndpoint = config.getApi_endpoint();
    }

    public List<List<Float>> createEmbedding(List<String> docs) {
        List<List<Float>> result = new ArrayList<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .build();
        try {
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            OpenAIEmbeddingRequest openAIEmbeddingRequest = new OpenAIEmbeddingRequest();
            openAIEmbeddingRequest.setInput(docs);
            openAIEmbeddingRequest.setModel(this.modelName);
            String jsonBody = gson.toJson(openAIEmbeddingRequest);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(apiEndpoint)
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
