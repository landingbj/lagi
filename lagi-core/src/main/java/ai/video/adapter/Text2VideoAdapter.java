package ai.video.adapter;

import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.VideoGenerationResult;

public interface Text2VideoAdapter {

    VideoGenerationResult toVideo(ImageGenerationRequest request);
}
