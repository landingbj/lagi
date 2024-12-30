package ai.wrapper.impl;

import ai.wrapper.CommonAdapter;
import ai.wrapper.IWrapper;

import java.util.HashMap;
import java.util.Map;

public class LandingAdapter extends CommonAdapter implements IWrapper {
    @Override
    protected Map<String, String> getModelDriverMap() {
        Map<String, String> modelDriverMap = new HashMap<>();
        modelDriverMap.put("turing", "ai.llm.adapter.impl.LandingAdapter");
        modelDriverMap.put("qa", "ai.llm.adapter.impl.LandingAdapter");
        modelDriverMap.put("tree", "ai.llm.adapter.impl.LandingAdapter");
        modelDriverMap.put("proxy", "ai.llm.adapter.impl.LandingAdapter");
        modelDriverMap.put("cascade", "ai.llm.adapter.impl.LandingAdapter");

        modelDriverMap.put("image", "ai.image.adapter.impl.LandingImageAdapter");

        modelDriverMap.put("landing-tts", "ai.audio.adapter.impl.LandingAudioAdapter");
        modelDriverMap.put("landing-asr", "ai.audio.adapter.impl.LandingAudioAdapter");

        modelDriverMap.put("video", "ai.video.adapter.impl.LandingVideoAdapter");
        return modelDriverMap;
    }
}
