package ai.wrapper.impl;


import ai.wrapper.CommonAdapter;
import ai.wrapper.IWrapper;

import java.util.HashMap;
import java.util.Map;

public class CascadeAdapter extends CommonAdapter implements IWrapper {
    @Override
    protected Map<String, String> getModelDriverMap() {
        Map<String, String> modelDriverMap = new HashMap<>();

        modelDriverMap.put("cascade", "ai.llm.adapter.impl.LandingAdapter");

        modelDriverMap.put("cascade-image", "ai.image.adapter.impl.LandingImageCascadeAdapter");

        modelDriverMap.put("cascade-asr", "ai.audio.adapter.impl.LandingAudioCascadeAdapter");
        modelDriverMap.put("cascade-tts", "ai.audio.adapter.impl.LandingAudioCascadeAdapter");

        modelDriverMap.put("cascade-video", "ai.video.adapter.impl.LandingVideoCascadeAdapter");
        return modelDriverMap;
    }
}
