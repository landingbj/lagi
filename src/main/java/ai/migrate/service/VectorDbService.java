package ai.migrate.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.migrate.pojo.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.utils.HttpServiceCall;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class VectorDbService {
    private Gson gson = new Gson();

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
        String result = HttpServiceCall.httpPost(MigrateGlobal.DELETE_DOC_INDEX_URL, gson.toJson(idList));
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

    public AddDocsCustomResponse addIndexes(List<FileInfo> fileList) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        String result = HttpUtil.httpPost(MigrateGlobal.ADD_INDEXES_URL, header, fileList);
        return gson.fromJson(result, AddDocsCustomResponse.class);
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
