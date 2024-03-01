package ai.lagi.adapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.lagi.adapter.IImageGenerationAdapter;
import ai.learning.pojo.Response;
import ai.migrate.pojo.ImageGenerationRequest;
import ai.migrate.pojo.ImageGenerationResult;

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
