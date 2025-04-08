package ai.intent.impl;


import ai.embedding.Embeddings;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.pojo.IntentResult;
import ai.manager.EmbeddingManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.utils.CosineSimilarityUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class VectorIntentServiceImpl extends SampleIntentServiceImpl {



    @Override
    protected void setIntentByVector(ChatCompletionRequest chatCompletionRequest, Integer lIndex, String lastQ, IntentResult intentResult) {
        Embeddings embeddings = EmbeddingManager.getInstance().getAdapter();
        String lQ = chatCompletionRequest.getMessages().get(lIndex).getContent();
        List<Float> embedding1 = embeddings.createEmbedding(lQ);
        List<Float> embedding2 = embeddings.createEmbedding(lastQ);
        double v1 = CosineSimilarityUtil.calculateCosineSimilarity(embedding1, embedding2);
        log.info("lQ: {}\n lastQ: {} \n v1:{}", lQ, lastQ, v1);
        if(v1 > 0.6) {
            intentResult.setStatus(IntentStatusEnum.CONTINUE.getName());
            intentResult.setContinuedIndex(lIndex);
        } else {
            intentResult.setStatus(IntentStatusEnum.COMPLETION.getName());
        }
    }

}
