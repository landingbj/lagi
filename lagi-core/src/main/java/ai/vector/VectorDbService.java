package ai.vector;

import ai.common.pojo.Configuration;
import ai.common.pojo.IndexSearchData;
import ai.medusa.utils.LCS;
import ai.openai.pojo.ChatCompletionRequest;
import ai.utils.PriorityWordUtil;
import ai.utils.qa.ChatCompletionUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VectorDbService {
    private VectorStoreService vectorStoreService;

    public VectorDbService(Configuration config) {
//        Embeddings embeddingFunction = EmbeddingFactory.getEmbedding(config.getLLM().getEmbedding());
        vectorStoreService = new VectorStoreService();

    }
    public VectorDbService() {
        vectorStoreService = new VectorStoreService();
    }

    public boolean vectorStoreEnabled() {
        if (vectorStoreService != null) {
            return true;
        }
        return false;
    }

    public void deleteDoc(List<String> idList) {
        List<Map<String, String>> whereList = convert2WhereMap(idList);
        vectorStoreService.deleteWhere(whereList);
    }

    public void deleteDoc(List<String> idList, String category) {
        List<Map<String, String>> whereList = convert2WhereMap(idList);
        vectorStoreService.deleteWhere(whereList, category);
    }

    private List<Map<String, String>> convert2WhereMap(List<String> idList) {
        List<Map<String, String>> whereList = new ArrayList<>();
        for (String id : idList) {
            Map<String, String> where = new HashMap<>();
            where.put("file_id", id);
            whereList.add(where);
        }
        return whereList;
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
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        search = search.stream().filter(indexSearchData -> {
            String text = indexSearchData.getText();
            Set<String> longestCommonSubstrings = LCS.findLongestCommonSubstrings(lastMessage, text, 2);
            double ratio = LCS.getLcsRatio(lastMessage, longestCommonSubstrings);
            return ratio > 0.1;
        }).collect(Collectors.toList());
        return PriorityWordUtil.sortByPriorityWord(search);
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        vectorStoreService.addFileVectors(file, metadatas, category);
    }

    public List<String> getImageFiles(IndexSearchData indexData) {
        return vectorStoreService.getImageFiles(indexData);
    }
}
