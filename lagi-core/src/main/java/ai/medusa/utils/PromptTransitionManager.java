package ai.medusa.utils;

import ai.dao.asynchron.DefaultStateTransitionManager;

import java.util.Map;

public class PromptTransitionManager extends DefaultStateTransitionManager {
    public PromptTransitionManager() {
        TRANS_TABLE.put(PromptCacheConfig.POOL_INITIAL, PromptCacheConfig.POOL_DIVERSIFIED);
        TRANS_TABLE.put(PromptCacheConfig.POOL_DIVERSIFIED, PromptCacheConfig.POOL_CACHE_PUT);

        for (Map.Entry<Integer, Integer> e : TRANS_TABLE.entrySet()) {
            REVERSE_TABLE.put(e.getValue(), e.getKey());
        }
    }
}
