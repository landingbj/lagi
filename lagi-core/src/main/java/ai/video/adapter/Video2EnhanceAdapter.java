package ai.video.adapter;

import ai.common.pojo.VideoGenerationResult;

public interface Video2EnhanceAdapter {

    VideoGenerationResult enhance(String videoUrl);
}
