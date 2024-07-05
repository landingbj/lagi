package ai.video.adapter;

import ai.video.pojo.VideoEnhanceRequest;
import ai.video.pojo.VideoJobResponse;

public interface Video2EnhanceAdapter {

    VideoJobResponse enhance(VideoEnhanceRequest videoEnhanceRequest);
}
