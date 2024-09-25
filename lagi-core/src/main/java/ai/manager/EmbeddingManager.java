package ai.manager;

import ai.common.pojo.EmbeddingConfig;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddingManager {

    private final Logger log = LoggerFactory.getLogger(EmbeddingManager.class);

    private final Map<String, Embeddings> embeddingMap = new ConcurrentHashMap<>();

    private static final EmbeddingManager INSTANCE = new EmbeddingManager();

    public static EmbeddingManager getInstance() {
        return INSTANCE;
    }

    private EmbeddingManager(){}

    public void register(List<EmbeddingConfig> embeddings) {
        if(embeddings == null) {
            return;
        }
        embeddings.forEach(embeddingConfig -> {
            Embeddings embedding = EmbeddingFactory.getEmbedding(embeddingConfig);
            register(embeddingConfig.getType(), embedding);
        });
    }

    public void register(String key, Embeddings embeddings) {
        Embeddings temp = embeddingMap.putIfAbsent(key, embeddings);
        if (temp != null) {
            log.error("Adapter {} get {} is already exists!!", temp.getClass().getTypeName(), key);
        }
    }


    public Embeddings getAdapter(String key) {
        return embeddingMap.get(key);
    }

    public Embeddings getAdapter() {
        return embeddingMap.values().iterator().next();
    }

    public List<Embeddings> getAdapters() {
        return new ArrayList<>(embeddingMap.values());
    }

}


