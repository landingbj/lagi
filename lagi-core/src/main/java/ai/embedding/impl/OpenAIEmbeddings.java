package ai.embedding.impl;

import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.embedding.pojo.OpenAIEmbeddingResponse;
import ai.common.pojo.EmbeddingConfig;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenAIEmbeddings implements Embeddings {
    private String openAIAPIKey;
    private String modelName;
    private String apiEndpoint;
    private Gson gson = new Gson();

    public OpenAIEmbeddings(EmbeddingConfig config) {
        this.openAIAPIKey = config.getApi_key();
        this.modelName = config.getModel_name();
        this.apiEndpoint = config.getApi_endpoint();
    }

    public OpenAIEmbeddings(String openAIAPIKey, String modelName, String apiEndpoint) {
        this.openAIAPIKey = openAIAPIKey;
        this.modelName = modelName;
        this.apiEndpoint = apiEndpoint;
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


    public static OpenAIEmbeddings.Builder Builder() {
        return new OpenAIEmbeddings.Builder();
    }

    public static class Builder {
        private String openAIAPIKey;
        private String modelName;
        private String apiEndpoint;

        public Builder() {
        }

        public OpenAIEmbeddings build() {
            return new OpenAIEmbeddings(this.openAIAPIKey, this.modelName, this.apiEndpoint);
        }

        public OpenAIEmbeddings.Builder withOpenAIAPIKey(String openAIAPIKey) {
            this.openAIAPIKey = openAIAPIKey;
            return this;
        }

        public OpenAIEmbeddings.Builder withModelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public OpenAIEmbeddings.Builder withApiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
            return this;
        }
    }
}
