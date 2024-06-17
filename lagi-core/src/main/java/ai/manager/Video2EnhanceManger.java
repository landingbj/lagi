package ai.manager;

import ai.video.adapter.Video2EnhanceAdapter;

public class Video2EnhanceManger extends AIManager<Video2EnhanceAdapter>{
    private static final Video2EnhanceManger INSTANCE = new Video2EnhanceManger();
    public static Video2EnhanceManger getInstance() {
        return INSTANCE;
    }
    private Video2EnhanceManger(){}
}
