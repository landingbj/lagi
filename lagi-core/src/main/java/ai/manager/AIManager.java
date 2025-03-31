package ai.manager;

import ai.common.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AIManager<T> {


    private final Logger log = LoggerFactory.getLogger(AIManager.class);

    protected final Map<String, T> aiMap = new ConcurrentHashMap<>();


    public void register(String key, T adapter) {
        T tempAdapter = aiMap.putIfAbsent(key, adapter);
        if (tempAdapter != null) {
            log.error("Adapter {} name {} is already exists!!", adapter.getClass().getName(), key);
        }
    }


    public T getAdapter(String key) {
        return aiMap.get(key);
    }

    public T getAdapter() {
        if(aiMap.isEmpty()) {
            return null;
        }
        return aiMap.values().iterator().next();
    }

    public List<T> getAdapters() {
        return getDefaultSortedAdapter(aiMap);
    }


    public List<T> getAllAdapters() {
        return aiMap.values().stream().distinct().collect(Collectors.toList());
    }

    private static <T> List<T> getSortedAdapter(Map<String, T> map, Comparator<? super T> comparator) {
        return map.values().stream().distinct().filter(a->{
            ModelService m = (ModelService) a;
            if(!Boolean.TRUE.equals(m.getEnable())) {
                return false;
            }
            return m.getPriority() != null &&  m.getPriority() > 0;
        }).sorted(comparator).collect(Collectors.toList());
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
