package ai.video.service;

import ai.common.pojo.*;
import ai.manager.Image2VideoManager;
import ai.manager.Text2VideoManager;
import ai.manager.Video2EnhanceManger;
import ai.manager.Video2TrackManager;
import ai.video.adapter.Image2VideoAdapter;
import ai.video.adapter.Text2VideoAdapter;
import ai.video.adapter.Video2EnhanceAdapter;
import ai.video.adapter.Video2trackAdapter;
import ai.video.pojo.VideoEnhanceRequest;
import ai.video.pojo.VideoGeneratorRequest;
import ai.video.pojo.VideoJobResponse;
import ai.video.pojo.VideoTackRequest;

public class AllVideoService {

    public VideoJobResponse image2Video(VideoGeneratorRequest videoGeneratorRequest) {
        if(videoGeneratorRequest.getModel() != null) {
            Image2VideoAdapter adapter = Image2VideoManager.getInstance().getAdapter(videoGeneratorRequest.getModel());
            if(adapter != null) {
                return adapter.image2Video(videoGeneratorRequest);
            }
        }
        for(Image2VideoAdapter adapter: Image2VideoManager.getInstance().getAdapters()) {
            return adapter.image2Video(videoGeneratorRequest);
        }
        return null;
    }



    public VideoJobResponse enhance(VideoEnhanceRequest videoEnhanceRequest) {
        if(videoEnhanceRequest.getModel() != null) {
            Video2EnhanceAdapter adapter = Video2EnhanceManger.getInstance().getAdapter(videoEnhanceRequest.getModel());
            if(adapter != null) {
                return adapter.enhance(videoEnhanceRequest);
            }
        }
        for(Video2EnhanceAdapter adapter: Video2EnhanceManger.getInstance().getAdapters()) {
            return adapter.enhance(videoEnhanceRequest);
        }
        return null;
    }


    public VideoJobResponse track(VideoTackRequest videoTackRequest) {
        if(videoTackRequest.getModel() != null) {
            Video2trackAdapter adapter = Video2TrackManager.getInstance().getAdapter(videoTackRequest.getModel());
            if(adapter != null) {
                return adapter.track(videoTackRequest.getVideoUrl());
            }
        }
        for (Video2trackAdapter adapter: Video2TrackManager.getInstance().getAdapters()) {
            return adapter.track(videoTackRequest.getVideoUrl());
        }
        return null;
    }


    public VideoJobResponse toVideo(ImageGenerationRequest request) {
        for (Text2VideoAdapter adapter: Text2VideoManager.getInstance().getAdapters()) {
            return adapter.toVideo(request);
        }
        return null;
    }


}
