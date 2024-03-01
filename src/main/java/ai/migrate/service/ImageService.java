package ai.migrate.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.migrate.pojo.Response;
import ai.service.pojo.GenerateVideoRequest;
import ai.utils.FileUploadUtil;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;

public class ImageService {
    private Gson gson = new Gson();
    private AiServiceCall call = new AiServiceCall();
    
    public String enhanceImage(List<File> fileList) throws IOException {
        String fileParmName = "file";
        Map<String, String> formParmMap = new HashMap<>();
        String returnStr = HttpUtil.multipartUpload(
                MigrateGlobal.ESRGAN_URL + "/realesrgan/inference", fileParmName,
                fileList, formParmMap);
        if (returnStr == null) {
            return null;
        }
        Response response = gson.fromJson(returnStr, Response.class);
        if (response.getStatus().equals("success")) {
            return response.getData();
        }
        return null;
    }
    
    public String generateVideo(File file) {
        String imageUrl = FileUploadUtil.svdUpload(file);
        GenerateVideoRequest request = new GenerateVideoRequest();
        request.setImageUrl(imageUrl);

        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "generateVideoByImage", params);
        Response response = gson.fromJson(result[0], Response.class);
        if (response.getStatus().equals("success")) {
            return response.getData();
        }
        return null;
    }
}
