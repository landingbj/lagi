package ai.audio;

import ai.audio.adapter.IAudioAdapter;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager {
    private static final Map<String, IAudioAdapter> asrAdapters = new ConcurrentHashMap<>();
    private static final Map<String, IAudioAdapter> ttsAdapters = new ConcurrentHashMap<>();

    public static void registerASRAdapter(String name, IAudioAdapter adapter) {
        IAudioAdapter audioAdapter = asrAdapters.putIfAbsent(name, adapter);
        if (audioAdapter != null) {
            throw new RRException("Adapter " + name + " already exists");
        }
    }

    public static IAudioAdapter getASRAdapter(String name) {
        return asrAdapters.get(name);
    }

    public static void registerTTSAdapter(String name, IAudioAdapter adapter) {
        IAudioAdapter audioAdapter = ttsAdapters.putIfAbsent(name, adapter);
        if (audioAdapter != null) {
            throw new RRException("Adapter " + name + " already exists");
        }
    }

    public static IAudioAdapter getTTSAdapter(String name) {
        return ttsAdapters.get(name);
    }
}
