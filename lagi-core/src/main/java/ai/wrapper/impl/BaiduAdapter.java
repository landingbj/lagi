package ai.wrapper.impl;


import ai.wrapper.CommonAdapter;
import ai.wrapper.IWrapper;

import java.util.HashMap;
import java.util.Map;

public class BaiduAdapter extends CommonAdapter implements IWrapper {
    @Override
    protected Map<String, String> getModelDriverMap() {
        Map<String, String> modelDriverMap = new HashMap<>();
        modelDriverMap.put("ERNIE-Speed-128K", "ai.llm.adapter.impl.ErnieAdapter");
        modelDriverMap.put("ERNIE-Bot-turbo", "ai.llm.adapter.impl.ErnieAdapter");
        modelDriverMap.put("ERNIE-4.0-8K", "ai.llm.adapter.impl.ErnieAdapter");
        modelDriverMap.put("ERNIE-3.5-8K-0205", "ai.llm.adapter.impl.ErnieAdapter");
        modelDriverMap.put("ERNIE-3.5-4K-0205", "ai.llm.adapter.impl.ErnieAdapter");
        modelDriverMap.put("ERNIE-3.5-8K-1222", "ai.llm.adapter.impl.ErnieAdapter");

        modelDriverMap.put("Fuyu-8B", "ai.image.adapter.impl.BaiduImageAdapter");
        modelDriverMap.put("Stable-Diffusion-XL", "ai.image.adapter.impl.BaiduImageAdapter");

        modelDriverMap.put("translate", "ai.translate.adapter.impl.BaiduTranslateAdapter");

        modelDriverMap.put("enhance", "ai.image.adapter.impl.BaiduAiImageAdapter");

        modelDriverMap.put("aiVideo", "ai.video.adapter.impl.BaiduVideoAdapter");
        return modelDriverMap;
    }
}
