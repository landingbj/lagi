package ai.migrate.service;

import ai.common.client.AiServiceCall;
import ai.common.pojo.*;
import ai.image.service.AllImageService;
import ai.translate.TranslateService;
import ai.utils.*;
import ai.video.service.AllVideoService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ApiService {
    private Gson gson = new Gson();
    private ImageService imageService = new ImageService();
    private AllVideoService videoService = new AllVideoService();
    private AiServiceCall call = new AiServiceCall();
    private static Configuration config = MigrateGlobal.config;
    private AllImageService allImageService = new AllImageService();
    private TranslateService translateService = new TranslateService();
    
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
        String english = translateService.toEnglish(content);
        if(english != null) {
            request.setPrompt(english);
        } else {
            request.setPrompt(content);
        }
        ImageGenerationResult imageGenerationResult = allImageService.generations(request);
        AtomicReference<WhisperResponse> whisperResponse1 = new AtomicReference<>();
        Optional.ofNullable(imageGenerationResult).ifPresent(r->{
            if("base64".equals(r.getDataType())) {
                try {
                    File file = ImageUtil.base64ToFile(r.getData().get(0).getBase64Image());
                    File img = new File(filePath, file.getName());
                    FileUtils.copyFile(file, img);
                    WhisperResponse whisperResponse = new WhisperResponse(1, img.getName());
                    whisperResponse1.set(whisperResponse);
                } catch (Exception ignored) {

                }
            } else {
                String url = r.getData().get(0).getUrl();
                File tempDir = new File(filePath);
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                whisperResponse1.set(DownloadUtils.downloadFile(url, "png", filePath));
            }
        });
        return whisperResponse1.get();
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

        ImageEnhanceResult enhance = allImageService.enhance(imageUrl);
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(enhance.getEnhancedUrl(), "png",filePath);

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
        String imageUrl = videoService.image2Video(file.getAbsolutePath()).getUrl();
        
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
        ImageToTextResponse text = allImageService.toText(FileRequest.builder().imageUrl(file.getAbsolutePath()).build());
        if(text.getSamUrl() == null) {
            ServletContext context = req.getServletContext();
            String rootPath = context.getRealPath("");
            String filePath = rootPath + "static/img/split/";
            File tempDir = new File(filePath);
            if(!tempDir.exists()) {
                tempDir.mkdirs();
            }
            String filename = UUID.randomUUID() + ".png";
            FileUtils.copyFile(file, new File(filePath, filename));
            text.setSamUrl("static/img/split/" + filename);
        }
        if(text.getClassification() == null) {
            text.setClassification("");
        }

        String chinese = translateService.toChinese(text.getCaption());
        if(chinese != null) {
            text.setCaption(chinese);
        }
        return gson.toJson(text);
    }
    
    public String motInference(String lastVideoFile, HttpServletRequest req) throws IOException {
        File file = new File(lastVideoFile);
        VideoGenerationResult track = videoService.track(file.getAbsolutePath());
        JsonObject result = new JsonObject();
        if (track != null) {
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
        
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(track.getUrl(), "mp4", filePath);
        result.addProperty("data", "static/video/"+whisperResponse1.getMsg());
        result.addProperty("type", "mot");
        result.addProperty("status", "success");
        return gson.toJson(result);
    }
    
    public String mmeditingInference(String lastVideoFile, HttpServletRequest req) throws IOException {
        File file = new File(lastVideoFile);
        VideoGenerationResult enhance = videoService.enhance(file.getAbsolutePath());
        JsonObject result = new JsonObject();
        
        if (enhance == null) {
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
        
        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(enhance.getUrl(), "mp4", filePath);
        result.addProperty("data", "static/video/"+whisperResponse1.getMsg());
        result.addProperty("type", "mmediting");
        result.addProperty("status", "success");
        return gson.toJson(result);
    }
}
