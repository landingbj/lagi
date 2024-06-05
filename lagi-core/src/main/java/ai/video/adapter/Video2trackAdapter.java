package ai.video.adapter;

import ai.common.pojo.VideoGenerationResult;

public interface Video2trackAdapter {
    VideoGenerationResult track(String videoUrl);
}
