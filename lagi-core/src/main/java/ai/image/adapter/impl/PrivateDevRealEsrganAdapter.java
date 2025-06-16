package ai.image.adapter.impl;

import ai.annotation.ImgEnhance;
import ai.common.ModelService;
import ai.common.pojo.ImageEnhanceResult;
import ai.image.adapter.ImageEnhanceAdapter;
import ai.image.pojo.ImageEnhanceRequest;
import ai.utils.HttpUtil;
import ai.utils.ImageUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@ImgEnhance(company = "private-dev", modelNames = "realesrgan")
public class PrivateDevRealEsrganAdapter extends ModelService implements ImageEnhanceAdapter {

    @Override
    public ImageEnhanceResult enhance(ImageEnhanceRequest imageEnhanceRequest) {
        String imageUrl = imageEnhanceRequest.getImageUrl();
        if(imageUrl == null) {
            return null;
        }
        // 如果是网络连接，先下载保存到本地临时文件
        File tempFile = null;
        if ((imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            try {
                Path tempPath = Files.createTempFile("image-enhance",  ".jpg");
                tempFile = ImageUtil.urlToFile(imageUrl, tempPath.toFile().getAbsolutePath());
            } catch (IOException e) {
                return null;
            }
        }
        // 准备上传文件
        File uploadFile;
        if (tempFile != null) {
            uploadFile = tempFile;
        } else {
            uploadFile = new File(imageUrl); // 假设已经是本地路径
        }
        String enhanceResultStr;
        try {
            enhanceResultStr = HttpUtil.uploadFile(this.endpoint + "/enhance_image", uploadFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("enhance failed {}", e.getMessage());
            return null;
        }
        Gson gson = new Gson();

        Map<String, String> enhanceResult = gson.fromJson(enhanceResultStr, new TypeToken<Map<String, String>>() {
        }.getType());
        if(enhanceResult == null) {
            return null;
        }
        if(!"success".equals(enhanceResult.get("status"))) {
            log.error("enhance failed {}", enhanceResult.get("errorMessage"));
            return null;
        }
        String url = enhanceResult.get("image_url");
        if(StrUtil.isBlank(url)) {
            return null;
        }
        return ImageEnhanceResult.builder().data(this.endpoint + url).type("url").build();
    }




}
