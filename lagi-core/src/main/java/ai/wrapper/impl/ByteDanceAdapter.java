package ai.wrapper.impl;


import ai.wrapper.CommonAdapter;
import ai.wrapper.IWrapper;

import java.util.HashMap;
import java.util.Map;

public class ByteDanceAdapter extends CommonAdapter implements IWrapper {
    @Override
    protected Map<String, String> getModelDriverMap() {
        Map<String, String> modelDriverMap = new HashMap<>();
        modelDriverMap.put("doubao-pro-4k", "ai.llm.adapter.impl.DoubaoAdapter");
        modelDriverMap.put("doubao-pro-32k", "ai.llm.adapter.impl.DoubaoAdapter");

        modelDriverMap.put("openspeech", "ai.audio.adapter.impl.VolcEngineAudioAdapter");
        return modelDriverMap;
    }
}
