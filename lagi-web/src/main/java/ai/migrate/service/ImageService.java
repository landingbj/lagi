package ai.migrate.service;

import java.io.File;

import com.google.gson.Gson;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.common.pojo.Response;
import ai.common.pojo.GenerateVideoRequest;
import ai.utils.FileUploadUtil;

public class ImageService {
    private Gson gson = new Gson();
    private AiServiceCall call = new AiServiceCall();

    public String generateVideo(File file) {
        String imageUrl = FileUploadUtil.svdUpload(file);
        GenerateVideoRequest request = new GenerateVideoRequest();
        request.setImageUrl(imageUrl);

        Object[] params = {gson.toJson(request)};
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "generateVideoByImage", params);
        Response response = gson.fromJson(result[0], Response.class);
        if (response.getStatus().equals("success")) {
            return response.getData();
        }
        return null;
    }
}
