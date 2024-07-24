package ai.manager;


import ai.ocr.IOcr;

public class OcrManager extends AIManager<IOcr> {
    private static final OcrManager INSTANCE = new ai.manager.OcrManager();

    public static OcrManager getInstance() {
        return INSTANCE;
    }

    private OcrManager() {
    }
}
