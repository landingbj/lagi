package ai.manager;

import ai.llm.adapter.ILlmAdapter;
import ai.llm.adapter.impl.ProxyLlmAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LlmManager extends AIManager<ILlmAdapter>{

    private static final LlmManager  INSTANCE = new LlmManager();

    private LlmManager() {

    }

    public static LlmManager getInstance(){
        return INSTANCE;
    }

    @Override
    public void register(String key, ILlmAdapter adapter) {
        ProxyLlmAdapter proxyLlmAdapter = new ProxyLlmAdapter(adapter);
        ILlmAdapter tempAdapter = aiMap.putIfAbsent(key, proxyLlmAdapter);
        if (tempAdapter != null) {
            log.error("Adapter {} name {} is already exists!!", proxyLlmAdapter.getClass().getName(), key);
        }
    }
}
