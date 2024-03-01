package ai.migrate.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.migrate.pojo.ExtractContentResponse;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FileService {
    private Gson gson = new Gson();

    public ExtractContentResponse extractContentWithoutImage(File file) throws IOException {
        String fileParmName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        String returnStr = HttpUtil.multipartUpload(MigrateGlobal.EXTRACT_CONTENT_WITHOUT_IMAGE_URL, fileParmName, fileList, formParmMap);
        ExtractContentResponse response = gson.fromJson(returnStr, ExtractContentResponse.class);
        return response;
    }
    
    public ExtractContentResponse extractContent(String filepath) throws IOException {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        JsonObject payload = new JsonObject();
        payload.addProperty("filepath", filepath);
        String result = HttpUtil.httpPost(MigrateGlobal.EXTRACT_CONTENT_URL, header, payload);
        return gson.fromJson(result, ExtractContentResponse.class);
    }
}
