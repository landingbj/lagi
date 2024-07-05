package ai.video.adapter;

import ai.video.pojo.VideoJobResponse;

public interface Video2trackAdapter {
    VideoJobResponse track(String videoUrl);
}
