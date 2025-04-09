package ai.intent.impl;


import ai.embedding.Embeddings;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.pojo.IntentResult;
import ai.manager.EmbeddingManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.CosineSimilarityUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class VectorIntentServiceImpl extends SampleIntentServiceImpl {



    @Override
    protected void setIntentByVector(ChatCompletionRequest chatCompletionRequest, Integer lIndex, String lastQ, IntentResult intentResult) {
        Embeddings embeddings = EmbeddingManager.getInstance().getAdapter();
        List<Float> embedding2 = embeddings.createEmbedding(lastQ);
        for (int i = 0; i < chatCompletionRequest.getMessages().size() - 1; i++) {
            if(i < lIndex) {
                continue;
            }
            ChatMessage chatMessage = chatCompletionRequest.getMessages().get(i);
            if(!chatMessage.getRole().equals("user")) {
                continue;
            }
            List<Float> embedding1 = embeddings.createEmbedding(chatMessage.getContent());
            double v1 = CosineSimilarityUtil.calculateCosineSimilarity(embedding1, embedding2);
            log.info("lQ: {}\n lastQ: {} \n v1:{}", chatMessage.getContent(), lastQ, v1);
            if(v1 > 0.55) {
                intentResult.setStatus(IntentStatusEnum.CONTINUE.getName());
                intentResult.setContinuedIndex(lIndex);
                return;
            }
        }
        intentResult.setStatus(IntentStatusEnum.COMPLETION.getName());
    }

}
