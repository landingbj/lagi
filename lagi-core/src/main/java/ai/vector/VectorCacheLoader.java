package ai.vector;

import ai.common.pojo.IndexSearchData;
import ai.medusa.MedusaService;
import ai.medusa.utils.PromptCacheConfig;
import ai.vector.pojo.IndexRecord;
import com.google.common.cache.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VectorCacheLoader {
    private static final Logger logger = LoggerFactory.getLogger(VectorCacheLoader.class);
    private static final VectorCache vectorCache = VectorCache.getInstance();
    private static final VectorStoreService vectorStoreService = new VectorStoreService();
    private static final MedusaService medusaService = new MedusaService();
    private static LoadingCache<String, String> cacheL2;

    static {
        try {
            cacheL2 = loadCache(new CacheLoader<String, String>() {
                @Override
                public String load(@NotNull String key) throws Exception {
                    return "";
                }
            });
        } catch (Exception ignored) {
        }
    }

    private static LoadingCache<String, String> loadCache(CacheLoader<String, String> cacheLoader) throws Exception {
        return  CacheBuilder.newBuilder()
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> rn) {
                    }
                })
                .recordStats()
                .build(cacheLoader);
    }

    public static void load() {
        new Thread(() -> {
            try {
                logger.info("VectorCacheLoader started");
//                loadVectorLinkCache();
                if (PromptCacheConfig.MEDUSA_ENABLE) {
                    loadMedusaCache();
                }
                logger.info("VectorCacheLoader initialized");
            } catch (Exception e) {
                logger.error("VectorCacheLoader init error", e);
            }
        }).start();
    }

    private static void loadVectorLinkCache() {
        Map<String, String> where = new HashMap<>();
        where.put("filename", "");
        List<IndexRecord> indexRecordList = vectorStoreService.fetch(where);
        for (IndexRecord indexRecord : indexRecordList) {
            IndexSearchData indexSearchData = vectorStoreService.toIndexSearchData(indexRecord);
            IndexSearchData extendedIndexSearchData = vectorStoreService.extendText(indexSearchData);
            vectorCache.putToVectorLinkCache(indexSearchData.getId(), extendedIndexSearchData);
        }
    }

    private static void loadMedusaCache() {
        Map<String, String> where = new HashMap<>();
        where.put("filename", "");
        List<IndexRecord> indexRecordList = vectorStoreService.fetch(where);
        for (IndexRecord indexRecord : indexRecordList) {
            IndexSearchData indexSearchData = vectorStoreService.toIndexSearchData(indexRecord);
            if (indexSearchData.getParentId() != null) {
                IndexSearchData questionIndexData = vectorStoreService.getParentIndex(indexSearchData.getParentId());
                String text = questionIndexData.getText().replaceAll("\n", "");
                put2L2(text, indexSearchData.getText());
            }
        }

    }

    public static void put2L2(String key, String value) {
        try {
            cacheL2.put(key, value);
        } catch (Exception ignored) {
        }
    }

    public static String get2L2(String key) {
        try {
            return cacheL2.get(key);
        } catch (ExecutionException ignored) {

        }
        return null;
    }


    public static List<IndexSearchData> getFromL2Near(String question, int nearNum) {
        List<IndexSearchData> search = vectorStoreService.search(question, "chaoyang");
        if(search.size() > nearNum) {
            return search.subList(0, nearNum);
        }
        return search;
    }

}
