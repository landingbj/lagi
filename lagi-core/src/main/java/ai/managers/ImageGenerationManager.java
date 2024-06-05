package ai.managers;

import ai.image.adapter.IImageGenerationAdapter;

public class ImageGenerationManager extends AIManager<IImageGenerationAdapter>{

    private static final ImageGenerationManager INSTANCE = new ImageGenerationManager();
    private ImageGenerationManager() {

    }
    public static ImageGenerationManager getInstance() {
        return INSTANCE;
    }

}
