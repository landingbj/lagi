package ai.vector;

import ai.common.pojo.IndexSearchData;
import ai.utils.LRUCache;

public class VectorCache {
    private static final VectorCache INSTANCE = new VectorCache();
    private static final LRUCache<String, IndexSearchData> vectorLinkCache = new LRUCache<>(VectorStoreConstant.VECTOR_LINK_CACHE);
    private static final LRUCache<String, IndexSearchData> vectorCache = new LRUCache<>(VectorStoreConstant.VECTOR_LINK_CACHE);
    private static final LRUCache<String, IndexSearchData> vectorChildCache = new LRUCache<>(VectorStoreConstant.VECTOR_LINK_CACHE);

    private VectorCache() {
    }

    public static VectorCache getInstance() {
        return INSTANCE;
    }

    public IndexSearchData getFromVectorLinkCache(String id) {
        return vectorLinkCache.get(id);
    }

    public void putToVectorLinkCache(String id, IndexSearchData extendedIndexSearchData) {
        vectorLinkCache.put(id, extendedIndexSearchData);
    }

    public IndexSearchData getVectorCache(String id) {
        return vectorLinkCache.get(id);
    }

    public void putVectorCache(String id, IndexSearchData indexSearchData) {
        vectorLinkCache.put(id, indexSearchData);
    }

    public IndexSearchData getVectorChildCache(String id) {
        return vectorChildCache.get(id);
    }

    public void putVectorChildCache(String id, IndexSearchData indexSearchData) {
        vectorChildCache.put(id, indexSearchData);
    }
}
