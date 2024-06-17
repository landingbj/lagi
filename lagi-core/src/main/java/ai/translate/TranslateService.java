package ai.translate;

import ai.manager.TranslateManager;
import ai.translate.adapter.TranslateAdapter;

public class TranslateService {

    public String toEnglish(String text) {
        for (TranslateAdapter adapter : TranslateManager.getInstance().getAdapters()) {
            return adapter.toEnglish(text);
        }
        return null;
    }

    public String toChinese(String text) {
        for (TranslateAdapter adapter : TranslateManager.getInstance().getAdapters()) {
            return adapter.toChinese(text);
        }
        return null;
    }
}
