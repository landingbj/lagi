package ai.llm.utils;

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

    private static final long GUAVA_CACHE_SIZE = 100000;

    private static final long GUAVA_CACHE_DAY = 1;

    private static LoadingCache<String, Boolean> GLOBAL_CACHE = null;

    static {
        try {
            GLOBAL_CACHE = loadCache(new CacheLoader <String, Boolean>() {
                @Override
                public Boolean load(String key) throws Exception {
                    return Boolean.TRUE;
                }
            });
        } catch (Exception e) {
            log.error("Cache error", e);
        }
    }


    private static LoadingCache<String, Boolean> loadCache(CacheLoader<String, Boolean> cacheLoader) throws Exception {
        LoadingCache<String, Boolean> cache = CacheBuilder.newBuilder()
                .maximumSize(GUAVA_CACHE_SIZE)
                .expireAfterAccess(GUAVA_CACHE_DAY, TimeUnit.HOURS)
                .expireAfterWrite(GUAVA_CACHE_DAY, TimeUnit.HOURS)
                .removalListener(new RemovalListener <String, Boolean>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, Boolean> rn) {
                        //逻辑操作
                    }
                })
                .recordStats()
                .build(cacheLoader);
        return cache;
    }

    /**
     * 设置缓存值
     * 注: 若已有该key值，则会先移除(会触发removalListener移除监听器)，再添加
     *
     * @param key
     * @param value
     */
    public static void put(String key, Boolean value) {
        try {
            GLOBAL_CACHE.put(key, value);
        } catch (Exception e) {
            log.error("设置缓存值出错", e);
        }
    }


    public static void putAll(Map<? extends String, ? extends Boolean> map) {
        try {
            GLOBAL_CACHE.putAll(map);
        } catch (Exception e) {
            log.error("bath put error", e);
        }
    }


    public static Boolean get(String key) {
        Boolean token = Boolean.TRUE;
        try {
            token = GLOBAL_CACHE.get(key);
        } catch (Exception e) {
            log.error("cache error ", e);
        }
        return token;
    }


    public static void remove(Long key) {
        try {
            GLOBAL_CACHE.invalidate(key);
        } catch (Exception e) {
            log.error("remove cache error", e);
        }
    }


    public static void removeAll(Iterable<Long> keys) {
        try {
            GLOBAL_CACHE.invalidateAll(keys);
        } catch (Exception e) {
            log.error("batch remove cache error", e);
        }
    }


    public static void removeAll() {
        try {
            GLOBAL_CACHE.invalidateAll();
        } catch (Exception e) {
            log.error("remove all  cache error", e);
        }
    }


    public static long size() {
        long size = 0;
        try {
            size = GLOBAL_CACHE.size();
        } catch (Exception e) {
            log.error("get cache size error", e);
        }
        return size;
    }

    public static void main(String[] args) throws InterruptedException {
        CacheManager.put("aaa", false);
        Boolean s = CacheManager.get("aaa");
        System.out.println(s);
        Thread.sleep(1000);
        s = CacheManager.get("aaa");
        System.out.println(s);
        Thread.sleep(3000);
        s = CacheManager.get("aaa");
        System.out.println("3s + 1s : " +s);
    }
}