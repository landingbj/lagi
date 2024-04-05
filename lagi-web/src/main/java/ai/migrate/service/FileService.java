package ai.migrate.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.migrate.pojo.Document;
import ai.migrate.pojo.ExtractContentResponse;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;

import com.google.gson.Gson;

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

    public ExtractContentResponse extractContent(File file) throws IOException {
        String fileParmName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        String returnStr = HttpUtil.multipartUpload(MigrateGlobal.EXTRACT_CONTENT_URL, fileParmName, fileList, formParmMap);
        ExtractContentResponse response = gson.fromJson(returnStr, ExtractContentResponse.class);
        return response;
    }

    public List<Document> splitChunks(String content, int chunkSize) {
        List<Document> result = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            String text = content.substring(start, end).replaceAll("\\s+", " ");
            Document doc = new Document();
            doc.setText(text);
            result.add(doc);
            start = end;
        }
        return result;
    }
}
