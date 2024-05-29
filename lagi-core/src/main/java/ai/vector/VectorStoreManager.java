package ai.vector;

import ai.common.exception.RRException;
import ai.vector.impl.BaseVectorStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorStoreManager {

    public static Map<String, BaseVectorStore> vectors = new ConcurrentHashMap<>();

    public static void registerVectorStore(String name, BaseVectorStore vectorStore) {
        BaseVectorStore vs = vectors.putIfAbsent(name, vectorStore);
        if(vs != null) {
            throw new RRException("vector store "+ name +" already exists");
        }
    }

    public static BaseVectorStore getVectorStore(String name) {
        return vectors.get(name);
    }

    public static BaseVectorStore getVectorStore() {
        return vectors.values().iterator().next();
    }

}
