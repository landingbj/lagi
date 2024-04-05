package ai.image.adapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.image.adapter.IImageGenerationAdapter;
import ai.learning.pojo.Response;
import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;

public class LandingImageGenerationAdapter implements IImageGenerationAdapter {
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
}
