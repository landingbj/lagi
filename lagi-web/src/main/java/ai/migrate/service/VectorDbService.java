package ai.migrate.service;

import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.common.pojo.Configuration;
import ai.common.pojo.IndexSearchData;
import ai.common.pojo.VectorStoreConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.utils.PriorityWordUtil;
import ai.vector.VectorStoreService;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VectorDbService {
    private VectorStoreService vectorStoreService;

    public VectorDbService(Configuration config) {
        VectorStoreConfig vectorStoreConfig = config.getVectorStore();
        Embeddings embeddingFunction = EmbeddingFactory.getEmbedding(config.getLLM().getEmbedding());
        if (vectorStoreConfig != null) {
            vectorStoreService = new VectorStoreService(vectorStoreConfig, embeddingFunction);
        }
    }

    public boolean vectorStoreEnabled() {
        if (vectorStoreService != null) {
            return true;
        }
        return false;
    }

    public void deleteDoc(List<String> idList) {
        List<Map<String, String>> whereList = new ArrayList<>();
        for (String id : idList) {
            Map<String, String> where = new HashMap<>();
            where.put("file_id", id);
            whereList.add(where);
        }
        vectorStoreService.deleteWhere(whereList);
    }

    public List<IndexSearchData> search(String question, String category) {
        List<IndexSearchData> search = vectorStoreService.search(question, category);
        return PriorityWordUtil.sortByPriorityWord(search);
    }

    public List<IndexSearchData> search(ChatCompletionRequest request) {
        List<IndexSearchData> search = vectorStoreService.search(request);
        return PriorityWordUtil.sortByPriorityWord(search);
    }

    public List<IndexSearchData> searchByContext(ChatCompletionRequest request) {
        List<IndexSearchData> search = vectorStoreService.searchByContext(request);
        return PriorityWordUtil.sortByPriorityWord(search);
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        vectorStoreService.addFileVectors(file, metadatas, category);
    }
}
