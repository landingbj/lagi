package ai.video.adapter;

import ai.video.pojo.VideoJobQueryResponse;

public interface VideoQuery {

    VideoJobQueryResponse query(String jobId);

}
