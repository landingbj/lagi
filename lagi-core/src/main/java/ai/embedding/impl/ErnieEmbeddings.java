package ai.embedding.impl;

import ai.embedding.Embeddings;
import ai.common.pojo.EmbeddingConfig;
import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.core.auth.Auth;
import com.baidubce.qianfan.model.embedding.EmbeddingResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErnieEmbeddings implements Embeddings {
    private String apiKey;
    private String secretKey;

    public ErnieEmbeddings(EmbeddingConfig config) {
        this.apiKey = config.getApi_key();
        this.secretKey = config.getSecret_key();
    }

    @Override
    public List<List<Float>> createEmbedding(List<String> docs) {
        EmbeddingResponse response = new Qianfan(Auth.TYPE_OAUTH, apiKey, secretKey).embedding()
                .model("Embedding-V1")
                .input(docs)
                .execute();
        List<List<Float>> result = new ArrayList<>();
        response.getData().forEach(data -> {
            List<Float> embeddingList = new ArrayList<>();
            for (BigDecimal value : data.getEmbedding()) {
                embeddingList.add(value.floatValue());
            }
            result.add(embeddingList);
        });
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
