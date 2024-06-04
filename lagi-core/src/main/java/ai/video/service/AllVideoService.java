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

public class AllVideoService {

    public VideoGenerationResult image2Video(String imageUrl) {
        for(Image2VideoAdapter adapter: Image2VideoManager.getInstance().getAdapters()) {
            return adapter.image2Video(imageUrl);
        }
        return null;
    }


    public VideoGenerationResult enhance(String videoUrl) {
        for(Video2EnhanceAdapter adapter: Video2EnhanceManger.getInstance().getAdapters()) {
            return adapter.enhance(videoUrl);
        }
        return null;
    }


    public VideoGenerationResult track(String videoUrl) {
        for (Video2trackAdapter adapter: Video2TrackManager.getInstance().getAdapters()) {
            return adapter.track(videoUrl);
        }
        return null;
    }


    public VideoGenerationResult toVideo(ImageGenerationRequest request) {
        for (Text2VideoAdapter adapter: Text2VideoManager.getInstance().getAdapters()) {
            return adapter.toVideo(request);
        }
        return null;
    }


}
