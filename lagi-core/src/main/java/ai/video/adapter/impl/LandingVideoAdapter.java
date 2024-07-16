package ai.video.adapter.impl;

import ai.annotation.Img2Video;
import ai.annotation.Text2Video;
import ai.annotation.VideoEnhance;
import ai.annotation.VideoTrack;
import ai.common.ModelService;
import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.common.pojo.*;
import ai.oss.UniversalOSS;
import ai.video.adapter.Image2VideoAdapter;
import ai.video.adapter.Text2VideoAdapter;
import ai.video.adapter.Video2EnhanceAdapter;
import ai.video.adapter.Video2trackAdapter;
import ai.video.pojo.VideoEnhanceRequest;
import ai.video.pojo.VideoGeneratorRequest;
import ai.video.pojo.VideoJobResponse;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@VideoTrack(modelNames = "video")
@Text2Video(modelNames = "video")
@VideoEnhance(modelNames = "video")
@Img2Video(modelNames = "video")
public class LandingVideoAdapter extends ModelService implements Image2VideoAdapter, Video2EnhanceAdapter, Video2trackAdapter, Text2VideoAdapter {

    private final Gson gson = new Gson();
    private final AiServiceCall call = new AiServiceCall();
    private UniversalOSS universalOSS;

    @Override
    public VideoJobResponse image2Video(VideoGeneratorRequest videoGeneratorRequest) {
        File file = new File(videoGeneratorRequest.getInputFileList().get(0).getUrl());
        String url = universalOSS.upload("svd/" + file.getName(), file);
        GenerateVideoRequest request = new GenerateVideoRequest();
        request.setImageUrl(url);
        Object[] params = {gson.toJson(request)};
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "generateVideoByImage", params);
        Response response = gson.fromJson(result[0], Response.class);
        if (response.getStatus().equals("success")) {
            return VideoJobResponse.builder().data(response.getData()).build();
        }
        return null;
    }

    @Override
    public VideoJobResponse enhance(VideoEnhanceRequest videoEnhanceRequest) {
        File file = new File(videoEnhanceRequest.getVideoURL());
        String url = universalOSS.upload("mmediting/" + file.getName(), file);
        MmeditingInferenceRequest request = new MmeditingInferenceRequest();
        request.setVideoUrl(url);

        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "mmeditingInference", params);
        Response response = gson.fromJson(result[0], Response.class);
        if(response != null) {
            return VideoJobResponse.builder().data(response.getData()).build();
        }
        return null;
    }

    @Override
    public VideoJobResponse track(String videoUrl) {
        File file = new File(videoUrl);
        String url = universalOSS.upload("mmtracking/" + file.getName(), file);
        MotInferenceRequest request = new MotInferenceRequest();
        request.setVideoUrl(url);
        Object[] params = { gson.toJson(request) };
        String[] result = call.callWS(AiServiceInfo.WSVdoUrl, "motInference", params);
        Response response = gson.fromJson(result[0], Response.class);
        if(response != null) {
            return VideoJobResponse.builder().status(response.getStatus()).data(response.getData()).build();
        }
        return null;
    }

    @Override
    public VideoJobResponse toVideo(ImageGenerationRequest request) {
        ImageGenerationResult generations = generations(request);
        if(generations != null) {
            String url = generations.getData().get(0).getUrl();
            return VideoJobResponse.builder().data(url).build();
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
        ImageGenerationData data = new ImageGenerationData();
        data.setUrl(response.getData());
        List<ImageGenerationData> datas = new ArrayList<>();
        datas.add(data);
        ImageGenerationResult result = new ImageGenerationResult();
        result.setCreated(System.currentTimeMillis() / 1000L);
        result.setData(datas);
        return result;
    }
}
