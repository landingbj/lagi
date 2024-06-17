package ai.manager;

import ai.video.adapter.Text2VideoAdapter;

public class Text2VideoManager extends AIManager<Text2VideoAdapter>{
    private static final Text2VideoManager INSTANCE = new Text2VideoManager();
    public static Text2VideoManager getInstance() {
        return INSTANCE;
    }
    private Text2VideoManager() {}
}
