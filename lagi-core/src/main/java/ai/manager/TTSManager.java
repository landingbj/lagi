package ai.manager;

import ai.audio.adapter.IAudioAdapter;

public class TTSManager extends AIManager<IAudioAdapter> {
    private static final TTSManager INSTANCE = new TTSManager();
    public static TTSManager getInstance() {
        return INSTANCE;
    }
    private TTSManager() {}
}
