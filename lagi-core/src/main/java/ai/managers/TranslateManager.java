package ai.managers;

import ai.translate.adapter.TranslateAdapter;

public class TranslateManager extends AIManager<TranslateAdapter>{
    private static final TranslateManager INSTANCE = new TranslateManager();
    public static TranslateManager getInstance() {
        return INSTANCE;
    }
    private TranslateManager() {}
}
