package ai.video.adapter;

import ai.common.pojo.VideoGenerationResult;

public interface Image2VideoAdapter {

    VideoGenerationResult image2Video(String imageUrl);
}
