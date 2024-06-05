package ai.image.adapter.impl;

import java.util.ArrayList;
import java.util.List;

import ai.common.pojo.EnhanceImageRequest;
import ai.common.pojo.ImageEnhanceResult;
import ai.image.adapter.ImageEnhanceAdapter;
import com.google.gson.Gson;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.image.adapter.IImageGenerationAdapter;
import ai.learning.pojo.Response;
import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;

public class LandingImageAdapter implements IImageGenerationAdapter, ImageEnhanceAdapter {
    private Gson gson = new Gson();
    private AiServiceCall call = new AiServiceCall();

    @Override
    public ImageGenerationResult generations(ImageGenerationRequest request) {
        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSImgUrl, "generations", params);
        Response response = gson.fromJson(result[0], Response.class);
        return toImageGenerationResult(response);
    }

    private ImageGenerationResult toImageGenerationResult(Response response) {
        ImageGenerationResult.Data data = new ImageGenerationResult.Data();
        data.setUrl(response.getData());
        List<ImageGenerationResult.Data> datas = new ArrayList<>();
        datas.add(data);
        ImageGenerationResult result = new ImageGenerationResult();
        result.setCreated(System.currentTimeMillis() / 1000L);
        result.setData(datas);
        return result;
    }

    @Override
    public ImageEnhanceResult enhance(String imageUrl) {
        EnhanceImageRequest request = new EnhanceImageRequest();
        request.setImageUrl(imageUrl);
        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSImgUrl, "enhanceImage", params);
        ai.common.pojo.Response response = gson.fromJson(result[0], ai.common.pojo.Response.class);
        return ImageEnhanceResult.builder().enhancedUrl(response.getData()).build();
    }
}
