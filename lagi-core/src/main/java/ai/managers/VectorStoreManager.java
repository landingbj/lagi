package ai.managers;

import ai.common.ModelService;
import ai.common.pojo.Backend;
import ai.common.pojo.EmbeddingConfig;
import ai.common.pojo.VectorStoreConfig;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.vector.VectorStore;
import ai.vector.impl.BaseVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VectorStoreManager {

    private final Logger log = LoggerFactory.getLogger(VectorStoreManager.class);

    private final Map<String, VectorStore> aiMap = new ConcurrentHashMap<>();

    private static final VectorStoreManager INSTANCE = new VectorStoreManager();

    public static VectorStoreManager getInstance() {
        return INSTANCE;
    }

    private VectorStoreManager(){}

    public void register(List<VectorStoreConfig> vectorStoreConfigs, List<Backend> backends, List<EmbeddingConfig> embeddings) {

        Map<String, VectorStoreConfig> vectorMap = vectorStoreConfigs.stream().collect(Collectors.toMap(VectorStoreConfig::getName, vectorStoreConfig -> vectorStoreConfig));
        backends.stream().filter(Backend::getEnable)
        .map(rc->vectorMap.get(rc.getBackend()))
        .filter(Objects::nonNull)
        .forEach(vectorStoreConfig -> {
            try {
                String name = vectorStoreConfig.getName();
                String driver = vectorStoreConfig.getDriver();
                Class<?> clazz = Class.forName(driver);
                Constructor<?> constructor = clazz.getConstructor(VectorStoreConfig.class, Embeddings.class);
                BaseVectorStore vs = (BaseVectorStore) constructor.newInstance(vectorStoreConfig, EmbeddingFactory.getEmbedding(embeddings.get(0)));
                register(name, vs);
            } catch (Exception e) {
                log.error("registerVectorStore ("+vectorStoreConfig.getName()+")error");
            }}
        );
    }

    public void register(String key, VectorStore adapter) {
        VectorStore temp = aiMap.putIfAbsent(key, adapter);
        if (temp != null) {
            log.error("Adapter {} name {} is already exists!!", adapter.getClass().getName(), key);
        }
    }


    public VectorStore getAdapter(String key) {
        return aiMap.get(key);
    }

    public VectorStore getAdapter() {
        return aiMap.values().iterator().next();
    }

    public List<VectorStore> getAdapters() {
        return getDefaultSortedAdapter(aiMap);
    }

    private static <T> List<T> getSortedAdapter(Map<String, T> map, Comparator<? super T> comparator) {
        return map.values().stream().sorted(comparator).collect(Collectors.toList());
    }

    private static <T> List<T> getDefaultSortedAdapter(Map<String, T> map) {
        return getSortedAdapter(map, (m1, m2)->{
            if(!(m1 instanceof ModelService)) {
                return 0;
            }
            ModelService ms1 = (ModelService)m1;
            ModelService ms2 = (ModelService)m2;
            if(ms1.getPriority() != null) {
                return ms1.getPriority().compareTo(ms2.getPriority()) * -1;
            }
            return 1;
        });
    }

}


