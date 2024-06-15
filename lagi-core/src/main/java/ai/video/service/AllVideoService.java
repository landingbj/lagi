package ai.video.service;

import ai.common.pojo.*;
import ai.managers.Image2VideoManager;
import ai.managers.Text2VideoManager;
import ai.managers.Video2EnhanceManger;
import ai.managers.Video2TrackManager;
import ai.video.adapter.Image2VideoAdapter;
import ai.video.adapter.Text2VideoAdapter;
import ai.video.adapter.Video2EnhanceAdapter;
import ai.video.adapter.Video2trackAdapter;
import ai.video.pojo.VideoEnhanceRequest;
import ai.video.pojo.VideoGeneratorRequest;
import ai.video.pojo.VideoJobResponse;

public class AllVideoService {

    public VideoJobResponse image2Video(VideoGeneratorRequest videoGeneratorRequest) {
        for(Image2VideoAdapter adapter: Image2VideoManager.getInstance().getAdapters()) {
            return adapter.image2Video(videoGeneratorRequest);
        }
        return null;
    }



    public VideoJobResponse enhance(VideoEnhanceRequest videoEnhanceRequest) {
        for(Video2EnhanceAdapter adapter: Video2EnhanceManger.getInstance().getAdapters()) {
            return adapter.enhance(videoEnhanceRequest);
        }
        return null;
    }


    public VideoJobResponse track(String videoUrl) {
        for (Video2trackAdapter adapter: Video2TrackManager.getInstance().getAdapters()) {
            return adapter.track(videoUrl);
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
