package ai.manager;

import ai.video.adapter.Image2VideoAdapter;

public class Image2VideoManager extends AIManager<Image2VideoAdapter>{
    private static final Image2VideoManager INSTANCE = new Image2VideoManager();
    public static Image2VideoManager getInstance() {
        return INSTANCE;
    }
    private Image2VideoManager() {}
}
