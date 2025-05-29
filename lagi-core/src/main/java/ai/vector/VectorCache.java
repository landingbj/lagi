package ai.vector;

import ai.common.pojo.IndexSearchData;
import ai.utils.LRUCache;

import java.util.List;

public class VectorCache {
    private static final VectorCache INSTANCE = new VectorCache();
    private static final LRUCache<String, IndexSearchData> vectorLinkCache = new LRUCache<>(VectorStoreConstant.VECTOR_LINK_CACHE);
    private static final LRUCache<IndexSearchData, List<IndexSearchData>> vectorListCache = new LRUCache<>(VectorStoreConstant.VECTOR_LINK_CACHE);

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

    public List<IndexSearchData> getFromVectorListCache(IndexSearchData indexSearchData) {
        return vectorListCache.get(indexSearchData);
    }

    public void putToVectorListCache(IndexSearchData indexSearchData, List<IndexSearchData> extendedIndexSearchData) {
        vectorListCache.put(indexSearchData, extendedIndexSearchData);
    }
}
