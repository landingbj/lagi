package ai.manager;

import ai.video.adapter.Video2trackAdapter;

public class Video2TrackManager extends AIManager<Video2trackAdapter>{
    private static final Video2TrackManager INSTANCE = new Video2TrackManager();
    public static Video2TrackManager getInstance() {
        return INSTANCE;
    }
    private Video2TrackManager() {}
}
