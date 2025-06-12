package ai.video.adapter.impl;

import ai.annotation.VideoEnhance;
import ai.common.ModelService;
import ai.utils.FileUtil;
import ai.utils.HttpUtil;
import ai.video.adapter.Video2EnhanceAdapter;
import ai.video.pojo.VideoEnhanceRequest;
import ai.video.pojo.VideoJobResponse;
import cn.hutool.core.util.StrUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@VideoEnhance(company = "privateDev", modelNames = "mmagic-cain")
public class PrivateDevMMagicAdapter extends ModelService  implements Video2EnhanceAdapter {

    @Override
    public VideoJobResponse enhance(VideoEnhanceRequest videoEnhanceRequest) {
        String videoURL = videoEnhanceRequest.getVideoURL();
        if(videoURL == null){
            return VideoJobResponse.builder().status("fail").message("videoURL is null").build();
        }
        File tempFile = null;
        if(videoURL.startsWith("http")){
            try {
                Path tempDirectory = Files.createTempDirectory("video-enhance");
                String absolutePath = Paths.get(tempDirectory.toString(), UUID.randomUUID().toString()).toFile().getAbsolutePath();
                tempFile = FileUtil.urlToFile(videoURL, absolutePath);
            } catch (IOException e) {
                return null;
            }
        }
        File uploadFile;
        if (tempFile != null) {
            uploadFile = tempFile;
        } else {
            uploadFile = new File(videoURL); // 假设已经是本地路径
        }
        String enhanceResultStr;
        try {
            enhanceResultStr = HttpUtil.uploadFile(this.endpoint + "/video_interpolation", uploadFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("video enhance failed {}", e.getMessage());
            return null;
        }
        Gson gson = new Gson();

        Map<String, String> enhanceResult = gson.fromJson(enhanceResultStr, new TypeToken<Map<String, String>>() {
        }.getType());
        if(enhanceResult == null) {
            return null;
        }
        if(!"success".equals(enhanceResult.get("status"))) {
            log.error("video enhance failed {}", enhanceResult.get("errorMessage"));
            return VideoJobResponse.builder().status("failed").message("视频增强失败, 可能是不支持的视频尺寸或格式").build();
        }
        String url = enhanceResult.get("url");
        if(StrUtil.isBlank(url)) {
            return null;
        }
        return VideoJobResponse.builder().data(this.endpoint + url).status("success").build();
    }
}
