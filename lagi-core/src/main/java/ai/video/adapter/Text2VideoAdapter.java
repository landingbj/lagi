package ai.video.adapter;

import ai.common.pojo.ImageGenerationRequest;
import ai.video.pojo.VideoJobResponse;

public interface Text2VideoAdapter {

    VideoJobResponse toVideo(ImageGenerationRequest request);
}
