package ai.migrate.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import ai.client.AiServiceCall;
import ai.migrate.pojo.Response;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;

public class AudioService {
	private AiServiceCall wsCall = new AiServiceCall();
	private Gson gson = new Gson();

    public String audio2text(List<File> fileList, String language) throws IOException {
        String fileParmName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        formParmMap.put("language", language);
        String returnStr = HttpUtil.multipartUpload(MigrateGlobal.WHISPER_URL + "/stt", fileParmName, fileList, formParmMap);
        if (returnStr == null) {
            return null;
        }
        Response response = gson.fromJson(returnStr, Response.class);
        if (response.getStatus().equals("success")) {
            return response.getData();
        }
        return null;
    }
	

}
