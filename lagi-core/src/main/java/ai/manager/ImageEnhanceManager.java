package ai.manager;

import ai.image.adapter.ImageEnhanceAdapter;

public class ImageEnhanceManager extends AIManager<ImageEnhanceAdapter> {
    private static final ImageEnhanceManager INSTANCE = new ImageEnhanceManager();
    public static ImageEnhanceManager getInstance() {
        return INSTANCE;
    }
    private ImageEnhanceManager() {}
}
