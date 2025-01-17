package ai.wrapper.impl;


import ai.wrapper.CommonAdapter;
import ai.wrapper.IWrapper;

import java.util.HashMap;
import java.util.Map;

public class FlytekAdapter extends CommonAdapter implements IWrapper {
    @Override
    protected Map<String, String> getModelDriverMap() {
        Map<String, String> modelDriverMap = new HashMap<>();
        modelDriverMap.put("v1.1", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("v2.1", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("v3.1", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("v3.5", "ai.llm.adapter.impl.SparkAdapter");
        modelDriverMap.put("v4.0", "ai.llm.adapter.impl.SparkAdapter");

        modelDriverMap.put("tti", "ai.image.adapter.impl.SparkImageAdapter");
        return modelDriverMap;
    }
}
