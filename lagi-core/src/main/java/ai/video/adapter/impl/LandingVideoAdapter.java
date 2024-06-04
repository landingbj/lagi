package ai.video.adapter.impl;

import ai.common.ModelService;
import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.common.pojo.*;
import ai.utils.FileUploadUtil;
import ai.video.adapter.Image2VideoAdapter;
import ai.video.adapter.Text2VideoAdapter;
import ai.video.adapter.Video2EnhanceAdapter;
import ai.video.adapter.Video2trackAdapter;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LandingVideoAdapter extends ModelService implements Image2VideoAdapter, Video2EnhanceAdapter, Video2trackAdapter, Text2VideoAdapter {

    private final Gson gson = new Gson();
    private final AiServiceCall call = new AiServiceCall();
    

    @Override
    public VideoGenerationResult image2Video(String imageUrl) {
        File file = new File(imageUrl);
        String url = FileUploadUtil.svdUpload(file);
        GenerateVideoRequest request = new GenerateVideoRequest();
        request.setImageUrl(url);
        Object[] params = {gson.toJson(request)};
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "generateVideoByImage", params);
        Response response = gson.fromJson(result[0], Response.class);
        if (response.getStatus().equals("success")) {
            return VideoGenerationResult.builder().url(response.getData()).build();
        }
        return null;
    }

    @Override
    public VideoGenerationResult enhance(String videoUrl) {
        File file = new File(videoUrl);
        String url = FileUploadUtil.mmeditingUpload(file);
        MmeditingInferenceRequest request = new MmeditingInferenceRequest();
        request.setVideoUrl(url);

        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "mmeditingInference", params);
        Response response = gson.fromJson(result[0], Response.class);
        if(response != null) {
            return VideoGenerationResult.builder().url(response.getData()).build();
        }
        return null;
    }

    @Override
    public VideoGenerationResult track(String videoUrl) {
        File file = new File(videoUrl);
        String url = FileUploadUtil.mmtrackingUpload(file);
        MotInferenceRequest request = new MotInferenceRequest();
        request.setVideoUrl(url);
        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "motInference", params);
        Response response = gson.fromJson(result[0], Response.class);
        if(response != null) {
            return VideoGenerationResult.builder().url(response.getData()).build();
        }
        return null;
    }

    @Override
    public VideoGenerationResult toVideo(ImageGenerationRequest request) {
        ImageGenerationResult generations = generations(request);
        if(generations != null) {
            String url = generations.getData().get(0).getUrl();
            return VideoGenerationResult.builder().url(url).build();
        }
        return null;
    }


    public ImageGenerationResult generations(ImageGenerationRequest request) {
        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSImgUrl, "generations", params);
        ai.learning.pojo.Response response = gson.fromJson(result[0], ai.learning.pojo.Response.class);
        return toImageGenerationResult(response);
    }

    private ImageGenerationResult toImageGenerationResult(ai.learning.pojo.Response response) {
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
