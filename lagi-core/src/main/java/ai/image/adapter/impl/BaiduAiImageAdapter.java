package ai.image.adapter.impl;

import ai.annotation.ImgEnhance;
import ai.common.ModelService;
import ai.common.pojo.ImageEnhanceResult;
import ai.image.adapter.ImageEnhanceAdapter;
import ai.image.pojo.ImageEnhanceRequest;
import ai.utils.BaiduHttpUtil;
import ai.utils.Base64Util;
import ai.utils.ImageUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;

@ImgEnhance(modelNames = "enhance")
public class BaiduAiImageAdapter extends ModelService implements ImageEnhanceAdapter {

    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        if(getSecretKey() == null || getSecretKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    private final Logger log = LoggerFactory.getLogger(BaiduAiImageAdapter.class);

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    private String getAk() throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(StrUtil.format("https://aip.baidubce.com/oauth/2.0/token?client_id={}&client_secret={}&grant_type=client_credentials",getApiKey(), getSecretKey()))
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        JSON parse = JSONUtil.parse(response.body().string());
        String accessToken = parse.getByPath("access_token", String.class);
        return accessToken;
    }

    @Override
    public ImageEnhanceResult enhance(ImageEnhanceRequest imageEnhanceRequest) {
        // 请求url
        String image = null;
        String url = "https://aip.baidubce.com/rest/2.0/image-process/v1/image_definition_enhance";
        try {
            byte[] imgData = ImageUtil.getFileStream(imageEnhanceRequest.getImageUrl());
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String param = "image=" + imgParam;
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = getAk();
            String result = BaiduHttpUtil.post(url, accessToken, param);
            image = JSONUtil.parse(result).getByPath("image", String.class);
        } catch (Exception e) {
            log.error("error", e);
        }
        return ImageEnhanceResult.builder().type("base64").data(image).build();
    }


}
