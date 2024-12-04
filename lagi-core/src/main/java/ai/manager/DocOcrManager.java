package ai.manager;


import ai.ocr.IOcr;

public class DocOcrManager extends AIManager<IOcr> {
    private static final DocOcrManager INSTANCE = new DocOcrManager();

    public static DocOcrManager getInstance() {
        return INSTANCE;
    }

    private DocOcrManager() {
    }
}
