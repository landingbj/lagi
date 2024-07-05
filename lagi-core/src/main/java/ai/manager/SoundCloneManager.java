package ai.manager;

import ai.audio.adapter.IAudioCloneAdapter;

public class SoundCloneManager extends AIManager<IAudioCloneAdapter>{
    private static final SoundCloneManager INSTANCE = new SoundCloneManager();
    public static SoundCloneManager getInstance() {
        return INSTANCE;
    }
    private SoundCloneManager() {}
}
