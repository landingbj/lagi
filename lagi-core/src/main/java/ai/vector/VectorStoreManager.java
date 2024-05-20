package ai.vector;

import ai.common.exception.RRException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorStoreManager {

    public static Map<String, VectorStore> vectors = new ConcurrentHashMap<>();

    public static void registerVectorStore(String name,VectorStore vectorStore) {
        VectorStore vs = vectors.putIfAbsent(name, vectorStore);
        if(vs != null) {
            throw new RRException("vector store "+ name +" already exists");
        }
    }

    public static VectorStore getVectorStore(String name) {
        return vectors.get(name);
    }

    public static VectorStore getVectorStore() {
        return vectors.values().iterator().next();
    }

}
