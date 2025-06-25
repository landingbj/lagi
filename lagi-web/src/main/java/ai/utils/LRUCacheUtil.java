package ai.utils;

import ai.dto.ProgressTrackerEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheUtil {
    private static final int MAX_CACHE_SIZE = 100; // 最大缓存大小

    private static Map<String, ProgressTrackerEntity> cache = new LinkedHashMap<String, ProgressTrackerEntity>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ProgressTrackerEntity> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static void put(String taskId, ProgressTrackerEntity tracker) {
        cache.put(taskId, tracker);
    }

    public static ProgressTrackerEntity get(String taskId) {
        return cache.get(taskId);
    }
}
