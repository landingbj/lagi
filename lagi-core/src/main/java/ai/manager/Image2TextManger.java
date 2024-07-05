package ai.manager;

import ai.image.adapter.IImage2TextAdapter;

public class Image2TextManger extends AIManager<IImage2TextAdapter>{
    private static final Image2TextManger INSTANCE = new Image2TextManger();
    public static Image2TextManger getInstance() {
        return INSTANCE;
    }
    private Image2TextManger() {}
}
