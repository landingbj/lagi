package ai.video.adapter.impl;

import ai.annotation.Text2Video;
import ai.common.ModelService;
import ai.common.pojo.ImageGenerationRequest;
import ai.translate.TranslateService;
import ai.utils.ApiInvokeUtil;
import ai.video.adapter.Text2VideoAdapter;
import ai.video.pojo.VideoJobResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Text2Video(company = "private-dev", modelNames = {"cogvideoX"})
public class PrivateDevCogvideoXAdapter extends ModelService  implements Text2VideoAdapter {

    @Override
    public VideoJobResponse toVideo(ImageGenerationRequest request) {
        if(request.getPrompt() == null || request.getPrompt().isEmpty()) {
            throw new RuntimeException("prompt is required");
        }
        Map<String, Object> body =  new HashMap<>();
        TranslateService translateService = new TranslateService();
        String english = null;
        try {
            english = translateService.toEnglish(request.getPrompt());
        } catch (Exception ignored) {

        }
        if(english == null) {
            english = request.getPrompt();
        }
        body.put("prompt", english);
        Gson gson = new Gson();
        String post = ApiInvokeUtil.post(endpoint + "/generate_video", null, gson.toJson(body), 10, TimeUnit.MINUTES);
        if(post == null) {
            return null;
        }
        Map<String, Object> map = gson.fromJson(post, new TypeToken<Map<String, Object>>() {
        });
        if(map == null) {
            return null;
        }
        String status = (String)map.get("status");
        if(!"success".equals(status)) {
            String errorMessage = (String) map.get("errorMessage");
            log.error("cogvideoX prompt {} errorMessage:{}", request.getPrompt(), errorMessage);
            return null;
        }
        String url = (String) map.get("url");
        if(url == null) {
            return null;
        }
        return VideoJobResponse.builder().data(endpoint +  url).build();
    }

}
