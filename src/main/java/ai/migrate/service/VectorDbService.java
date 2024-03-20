package ai.migrate.service;

import java.io.IOException;
import java.util.*;

import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.migrate.pojo.*;
import ai.vector.VectorStoreService;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class VectorDbService {
    private Gson gson = new Gson();

    private VectorStoreService vectorStoreService;

    public VectorDbService(Configuration config) {
        VectorStoreConfig vectorStoreConfig = config.getVector_store();
        Embeddings embeddingFunction = EmbeddingFactory.getEmbedding(config.getLLM().getEmbedding());
        vectorStoreService = new VectorStoreService(vectorStoreConfig, embeddingFunction);
    }

    public String addDocs(List<Document> docs) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.ADD_DOCS_INDEX_URL, header, docs);
        return result;
    }

    public String updateDocImage(List<Document> docs) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.UPDATE_DOC_IMAGE_URL, header, docs);
        return result;
    }

    public AddDocsCustomResponse addDocsCustom(List<Document> docs) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.ADD_DOCS_CUSTOM_URL, header, docs);
        return gson.fromJson(result, AddDocsCustomResponse.class);
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

    public String addDoc(Document doc) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.ADD_DOC_INDEX_URL, header, doc);
        return result;
    }

    public String search(IndexSearchRequest request) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.SEARCH_DOC_INDEX_URL, header, request);
        return result;
    }

    public List<IndexSearchData> search(String question) {
        int similarity_top_k = 1;
        double similarity_cutoff = 1;
        int parentDepth = 0;
        int childDepth = 0;
        Map<String, String> where = new HashMap<>();
        List<IndexSearchData> result = new ArrayList<>();
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where);
        for (IndexSearchData indexSearchData : indexSearchDataList) {
            result.add(extendText(parentDepth, childDepth, indexSearchData));
        }
        return result;
    }

    public IndexSearchData extendText(int parentDepth, int childDepth, IndexSearchData data) {
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

    public IndexSearchData getParentIndex(String parentId) {
        if (parentId == null) {
            return null;
        }
        return toIndexSearchData(vectorStoreService.fetch(parentId));
    }

    public IndexSearchData getChildIndex(String parentId) {
        IndexSearchData result = null;
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

    public List<IndexSearchData> search(String question, int similarity_top_k, double similarity_cutoff, Map<String, String> where) {
        List<IndexSearchData> result = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setText(question);
        queryCondition.setN(similarity_top_k);
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = vectorStoreService.query(queryCondition);
        for (IndexRecord indexRecord : indexRecords) {
            if (indexRecord.getDistance() > similarity_cutoff) {
                continue;
            }
            IndexSearchData indexSearchData = toIndexSearchData(indexRecord);
            result.add(indexSearchData);
        }
        return result;
    }

    private static IndexSearchData toIndexSearchData(IndexRecord indexRecord) {
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

    public String addInstruction(String json, String category) throws IOException {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        jsonObject.addProperty("category", category);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.ADD_INSTRUCTION_URL, header, jsonObject);
        return result;
    }

    public LcsResponse getAnswerByLcs(String question, String category) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("question", question);
        jsonObject.addProperty("category", category);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.GET_ANSWER_BY_LCS_URL, header, jsonObject);
        return gson.fromJson(result, LcsResponse.class);
    }

    public boolean isLcsRequest(String content) {
        if (isValidJson(content)) {
            JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
            if (jsonObject.has("question") && jsonObject.has("answer")) {
                return true;
            }
        }
        return false;
    }

    public void addIndexes(List<FileInfo> fileList) throws IOException {
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            upsertRecords.add(convertToUpsertRecord(fileInfo));
        }
        for (int i = 1; i < upsertRecords.size(); i++) {
            String parentId = upsertRecords.get(i - 1).getId();
            upsertRecords.get(i).getMetadata().put("parent_id", parentId);
        }
        vectorStoreService.upsert(upsertRecords);
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

    public boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            try {
                new JSONArray(json);
            } catch (JSONException ne) {
                return false;
            }
        }
        return true;
    }
}
