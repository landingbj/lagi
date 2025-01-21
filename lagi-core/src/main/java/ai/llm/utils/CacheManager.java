package ai.llm.utils;

import ai.config.ContextLoader;
import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

    private static final long GUAVA_CACHE_SIZE = 100000;

    private static final long GUAVA_CACHE_DAY = 1;

    private final long GUAVA_CACHE_SECONDS;

    private  LoadingCache<String, Boolean> GLOBAL_CACHE = null;

    private static final CacheManager INSTANCE = new CacheManager();

    private static final Map<String, Integer> COUNT_KEY = new ConcurrentHashMap<>();

    private CacheManager() {
        GUAVA_CACHE_SECONDS = ContextLoader.configuration.getFunctions().getChat().getGraceTime();
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
    public static CacheManager getInstance(){
        return INSTANCE;
    }



    private LoadingCache<String, Boolean> loadCache(CacheLoader<String, Boolean> cacheLoader) throws Exception {
        LoadingCache<String, Boolean> cache = CacheBuilder.newBuilder()
                .maximumSize(GUAVA_CACHE_SIZE)
//                .expireAfterAccess(GUAVA_CACHE_SECONDS, TimeUnit.SECONDS)
                .expireAfterWrite(GUAVA_CACHE_SECONDS, TimeUnit.SECONDS)
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
    public void put(String key, Boolean value) {
        try {
            GLOBAL_CACHE.put(key, value);
            COUNT_KEY.put(key, COUNT_KEY.getOrDefault(key, 0) + 1);
        } catch (Exception e) {
            log.error("设置缓存值出错", e);
        }
    }



    public void putAll(Map<? extends String, ? extends Boolean> map) {
        try {
            GLOBAL_CACHE.putAll(map);
        } catch (Exception e) {
            log.error("bath put error", e);
        }
    }


    public Boolean get(String key) {
        Boolean token = Boolean.TRUE;
        try {
            token = GLOBAL_CACHE.get(key);
        } catch (Exception e) {
            log.error("cache error ", e);
        }
        return token;
    }

    public Integer getCount(String key) {
        return COUNT_KEY.getOrDefault(key, 0);
    }


    public void remove(String key) {
        try {
            GLOBAL_CACHE.invalidate(key);
        } catch (Exception e) {
            log.error("remove cache error", e);
        }
    }

    public void removeCount(String key) {
        try {
            COUNT_KEY.remove(key);
        } catch (Exception e) {
            log.error("remove count error", e);
        }
    }


    public void removeAll(Iterable<Long> keys) {
        try {
            GLOBAL_CACHE.invalidateAll(keys);
        } catch (Exception e) {
            log.error("batch remove cache error", e);
        }
    }


    public void removeAll() {
        try {
            GLOBAL_CACHE.invalidateAll();
        } catch (Exception e) {
            log.error("remove all  cache error", e);
        }
    }


    public long size() {
        long size = 0;
        try {
            size = GLOBAL_CACHE.size();
        } catch (Exception e) {
            log.error("get cache size error", e);
        }
        return size;
    }

}