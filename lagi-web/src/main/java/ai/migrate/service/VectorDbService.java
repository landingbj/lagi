package ai.migrate.service;

import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.common.pojo.Configuration;
import ai.common.pojo.FileInfo;
import ai.common.pojo.IndexSearchData;
import ai.common.pojo.VectorStoreConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorStoreService;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VectorDbService {
    private Gson gson = new Gson();

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

    public String deleteDoc(List<String> idList) throws IOException {
        List<String> whereList = new ArrayList<>();
        for (String id : idList) {
            Map<String, String> whereMap = new HashMap<>();
            whereMap.put("file_id", id);
            String where = gson.toJson(whereMap);
            whereList.add(where);
        }
        Map<String, String> header = new HashMap<>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.DELETE_DOC_INDEX_URL, header, whereList);
        return result;
    }

    public List<IndexSearchData> search(ChatCompletionRequest request) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        List<IndexSearchData> indexSearchDataList = search(lastMessage, request.getCategory());
        return indexSearchDataList;
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        vectorStoreService.addFileVectors(file, metadatas, category);
    }

    private IndexSearchData extendText(int parentDepth, int childDepth, IndexSearchData data) {
        String text = data.getText();
        String parentId = data.getParentId();
        int parentCount = 0;
        for (int i = 0; i < parentDepth; i++) {
            IndexSearchData parentData = getParentIndex(parentId);
            if (parentData != null) {
                text = parentData.getText() + text;
                parentId = parentData.getParentId();
                parentCount++;
            } else {
                break;
            }
        }
        if (parentCount < parentDepth) {
            childDepth = childDepth + parentDepth - parentCount;
        }
        parentId = data.getId();
        for (int i = 0; i < childDepth; i++) {
            IndexSearchData childData = getChildIndex(parentId);
            if (childData != null) {
                text = text + childData.getText();
                parentId = childData.getId();
            } else {
                break;
            }
        }
        data.setText(text);
        return data;
    }

    private IndexSearchData getParentIndex(String parentId) {
        if (parentId == null) {
            return null;
        }
        return toIndexSearchData(vectorStoreService.fetch(parentId));
    }

    private IndexSearchData getChildIndex(String parentId) {
        IndexSearchData result = null;
        if (parentId == null) {
            return null;
        }
        Map<String, String> where = new HashMap<>();
        where.put("parent_id", parentId);
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = vectorStoreService.query(queryCondition);
        if (indexRecords != null && !indexRecords.isEmpty()) {
            result = toIndexSearchData(indexRecords.get(0));
        }
        return result;
    }

    public List<IndexSearchData> search(String question, String category) {
        int similarity_top_k = 1;
        double similarity_cutoff = 0.5;
        int parentDepth = 2;
        int childDepth = 2;
        Map<String, String> where = new HashMap<>();
        List<IndexSearchData> result = new ArrayList<>();
        category = ObjectUtils.defaultIfNull(category, "");
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where, category);
        for (IndexSearchData indexSearchData : indexSearchDataList) {
            result.add(extendText(parentDepth, childDepth, indexSearchData));
        }
        return result;
    }

    private List<IndexSearchData> search(String question, int similarity_top_k, double similarity_cutoff,
                                         Map<String, String> where, String category) {
        List<IndexSearchData> result = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setText(question);
        queryCondition.setN(similarity_top_k);
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = vectorStoreService.query(queryCondition, category);
        for (IndexRecord indexRecord : indexRecords) {
            if (indexRecord.getDistance() > similarity_cutoff) {
                continue;
            }
            IndexSearchData indexSearchData = toIndexSearchData(indexRecord);
            result.add(indexSearchData);
        }
        return result;
    }

    private IndexSearchData toIndexSearchData(IndexRecord indexRecord) {
        if (indexRecord == null) {
            return null;
        }
        IndexSearchData indexSearchData = new IndexSearchData();
        indexSearchData.setId(indexRecord.getId());
        indexSearchData.setText(indexRecord.getDocument());
        indexSearchData.setFileId((String) indexRecord.getMetadata().get("file_id"));
        indexSearchData.setCategory((String) indexRecord.getMetadata().get("category"));
        indexSearchData.setFilename(Collections.singletonList((String) indexRecord.getMetadata().get("filename")));
        indexSearchData.setFilepath(Collections.singletonList((String) indexRecord.getMetadata().get("filepath")));
        indexSearchData.setImage((String) indexRecord.getMetadata().get("image"));
        indexSearchData.setDistance(indexRecord.getDistance());
        indexSearchData.setParentId((String) indexRecord.getMetadata().get("parent_id"));
        return indexSearchData;
    }

    public void addIndexes(List<FileInfo> fileList, String category) throws IOException {
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            upsertRecords.add(convertToUpsertRecord(fileInfo));
        }
        for (int i = 1; i < upsertRecords.size(); i++) {
            String parentId = upsertRecords.get(i - 1).getId();
            upsertRecords.get(i).getMetadata().put("parent_id", parentId);
        }
        vectorStoreService.upsert(upsertRecords, category);
    }

    private UpsertRecord convertToUpsertRecord(FileInfo fileInfo) {
        UpsertRecord upsertRecord = new UpsertRecord();
        upsertRecord.setDocument(fileInfo.getText());
        upsertRecord.setId(fileInfo.getEmbedding_id());

        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, Object> entry : fileInfo.getMetadatas().entrySet()) {
            metadata.put(entry.getKey(), entry.getValue().toString());
        }
        upsertRecord.setMetadata(metadata);

        return upsertRecord;
    }
}
