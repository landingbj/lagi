package ai.video.adapter;

import ai.video.pojo.VideoGeneratorRequest;
import ai.video.pojo.VideoJobResponse;

public interface Image2VideoAdapter {

    VideoJobResponse image2Video(VideoGeneratorRequest videoGeneratorRequest);

}
