package ai.manager;

import ai.vl.adapter.VlAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VlLlmManager extends AIManager<VlAdapter>{

    private static final VlLlmManager INSTANCE = new VlLlmManager();

    private VlLlmManager() {

    }

    public static VlLlmManager getInstance(){
        return INSTANCE;
    }

    @Override
    public void register(String key, VlAdapter adapter) {
        VlAdapter tempAdapter = aiMap.putIfAbsent(key, adapter);
        if (tempAdapter != null) {
            log.error("Adapter {} name {} is already exists!!", adapter.getClass().getName(), key);
        }
    }
}
