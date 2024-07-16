package ai.vector;

import ai.common.pojo.IndexSearchData;
import ai.medusa.MedusaService;
import ai.medusa.utils.PromptCacheConfig;
import ai.utils.LagiGlobal;
import ai.vector.pojo.IndexRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorCacheLoader {
    private static final Logger logger = LoggerFactory.getLogger(VectorCacheLoader.class);
    private static final VectorCache vectorCache = VectorCache.getInstance();
    private static final VectorStoreService vectorStoreService = new VectorStoreService();
    private static final MedusaService medusaService = new MedusaService();

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
        Map<String, String> qaMap = new HashMap<>();
        for (IndexRecord indexRecord : indexRecordList) {
            IndexSearchData indexSearchData = vectorStoreService.toIndexSearchData(indexRecord);
            if (indexSearchData.getParentId() != null) {
                IndexSearchData questionIndexData = vectorStoreService.getParentIndex(indexSearchData.getParentId());
                qaMap.put(questionIndexData.getText(), indexSearchData.getText());
            }
        }
        medusaService.load(qaMap, LagiGlobal.getDefaultCategory());
    }
}
