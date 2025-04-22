package ai.wrapper.impl;


import ai.wrapper.CommonAdapter;
import ai.wrapper.IWrapper;

import java.util.HashMap;
import java.util.Map;

public class FlytekAdapter extends CommonAdapter implements IWrapper {
    @Override
    protected Map<String, String> getModelDriverMap() {
        Map<String, String> modelDriverMap = new HashMap<>();
        modelDriverMap.put("lite", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("generalv3", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("pro-128k", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("generalv3.5", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("max-32k", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("4.0Ultra", "ai.llm.adapter.impl.SparkAdapter");

        modelDriverMap.put("tti", "ai.image.adapter.impl.SparkImageAdapter");
        return modelDriverMap;
    }
}
