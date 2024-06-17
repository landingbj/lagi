package ai.manager;

import ai.audio.adapter.IAudioAdapter;

public class ASRManager extends AIManager<IAudioAdapter> {
    private static final ASRManager INSTANCE = new ASRManager();
    public static ASRManager getInstance() {
        return INSTANCE;
    }
    private ASRManager() {}
}
