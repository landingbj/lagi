package ai.migrate.service;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.lagi.service.ImageGenerationService;
import ai.migrate.pojo.*;
import ai.utils.DownloadUtils;
import ai.utils.FileUploadUtil;
import ai.utils.LagiGlobal;
import ai.utils.WhisperResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiService {
    private Gson gson = new Gson();
    private ImageService imageService = new ImageService();
    private VideoService videoService = new VideoService();
    private AiServiceCall call = new AiServiceCall();
    private static Configuration config = LagiGlobal.config;
    private ImageGenerationService imageGenerationService = new ImageGenerationService(config);
    
    public String generateImage(String content, HttpServletRequest req) throws IOException {
        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath + "static/img/txt2img/";

        WhisperResponse whisperResponse1 = generateImage(content, filePath);

        Map<String, String> map = new HashMap<>();
        map.put("status", "success");
        map.put("result", "static/img/txt2img/" + whisperResponse1.getMsg());
        String result = gson.toJson(map);
        return result;
    }
    
    public WhisperResponse generateImage(String content, String filePath) throws IOException {
        ImageGenerationRequest request = new ImageGenerationRequest();
        request.setPrompt(content);
        ImageGenerationResult imageGenerationResult = imageGenerationService.generations(request);
        
        String url = imageGenerationResult.getData().get(0).getUrl();

        File tempDir = new File(filePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        WhisperResponse whisperResponse1 = DownloadUtils.downloadFile(url, "png", filePath);
        return whisperResponse1;
    }
    
    public String enhanceImage(String lastImageFile, HttpServletRequest req) throws IOException {
        File file = new File(lastImageFile);
        String imageUrl = FileUploadUtil.enhanceImageUpload(file);

        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath + "static/img/enhance/";
        File tempDir = new File( filePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        EnhanceImageRequest request = new EnhanceImageRequest();
        request.setImageUrl(imageUrl);
        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSImgUrl, "enhanceImage", params);
        Response response = gson.fromJson(result[0], Response.class);
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(response.getData(), "png",filePath);

        JsonObject finalResult = new JsonObject();
        finalResult.addProperty("enhanceImageUrl", "static/img/enhance/" + whisperResponse1.getMsg());
        finalResult.addProperty("status", "success");
        return gson.toJson(finalResult);
    }

    public String generateVideoByText(String content, HttpServletRequest req) throws IOException {
        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath + "static/img/txt2img/";

        WhisperResponse whisperResponse1 = generateImage(content.replaceAll("视频", "图片"), filePath);
        String imageFile = filePath + whisperResponse1.getMsg();
        return generateVideo(imageFile, req);
    }

    public String generateVideo(String lastImageFile, HttpServletRequest req) throws IOException {
        File file = new File(lastImageFile);
        String imageUrl = imageService.generateVideo(file);
        
        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath + "static/img/svd/";
        File tempDir = new File( filePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(imageUrl, "mp4", filePath);
        JsonObject result = new JsonObject();
        result.addProperty("svdVideoUrl", "static/img/svd/" + whisperResponse1.getMsg());
        result.addProperty("status", "success");
        return gson.toJson(result);
    }
    
    public String imageToText(String lastImageFile, HttpServletRequest req) throws IOException {
        File file = new File(lastImageFile);
        String url = FileUploadUtil.imageCaptioningUpload(file);
        
        CaptionRequest request = new CaptionRequest();
        request.setImageUrl(url);

        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath + "static/img/split/";
        File tempDir = new File( filePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSImgUrl, "caption", params);
        ImageToTextResponse response = gson.fromJson(result[0], ImageToTextResponse.class);
        WhisperResponse whisperResponse = DownloadUtils.downloadFile(response.getSamUrl(), "png", filePath);
        response.setSamUrl("static/img/split/" + whisperResponse.getMsg());
        return gson.toJson(response);
    }
    
    public String motInference(String lastVideoFile, HttpServletRequest req) throws IOException {
        File file = new File(lastVideoFile);
        Response respose = videoService.motInference(file);
        JsonObject result = new JsonObject();
        
        if (respose == null) {
            result.addProperty("status", "failed");
            return gson.toJson(result);
        }
        
        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath+"static/video/";
        File tempDir = new File( filePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(respose.getData(), "mp4", filePath);
        result.addProperty("data", "static/video/"+whisperResponse1.getMsg());
        result.addProperty("type", "mot");
        result.addProperty("status", "success");
        return gson.toJson(result);
    }
    
    public String mmeditingInference(String lastVideoFile, HttpServletRequest req) throws IOException {
        File file = new File(lastVideoFile);
        Response respose = videoService.mmeditingInference(file);
        JsonObject result = new JsonObject();
        
        if (respose == null) {
            result.addProperty("status", "failed");
            return gson.toJson(result);
        }
        
        ServletContext context = req.getServletContext();
        String rootPath = context.getRealPath("");
        String filePath = rootPath+"static/video/";
        File tempDir = new File( filePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(respose.getData(), "mp4", filePath);
        result.addProperty("data", "static/video/"+whisperResponse1.getMsg());
        result.addProperty("type", "mmediting");
        result.addProperty("status", "success");
        return gson.toJson(result);
    }
}
