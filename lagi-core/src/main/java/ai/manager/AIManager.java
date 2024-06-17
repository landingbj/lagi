package ai.manager;

import ai.common.ModelService;
import ai.common.pojo.Backend;
import ai.common.pojo.Driver;
import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AIManager<T> {


    private final Logger log = LoggerFactory.getLogger(AIManager.class);

    private final Map<String, T> aiMap = new ConcurrentHashMap<>();

    public void register(List<Backend> models, List<Backend> functions) {
        if(functions == null) {
            return;
        }
        models.forEach(model->{
            if(model.getModel() != null && model.getDriver() != null) {
                model.setDrivers(Lists.newArrayList(new Driver(model.getModel(), model.getDriver())));
            }
        });
        Map<String, Backend> modelMap = models.stream().collect(Collectors.toMap(Backend::getName, model -> model));
        functions.stream().filter(Backend::getEnable).forEach(func->{
            Backend model =  modelMap.get(func.getBackend());
            if(model == null) {
                log.error("model {} not exist", func.getBackend());
                return ;
            }
            for (Driver driver : model.getDrivers()) {
                Set<String> modelSet = Arrays.stream(driver.getModel().split(",")).map(String::trim).collect(Collectors.toSet());
                if(modelSet.contains(func.getModel())) {
                    try {
                        Class<?> clazz = Class.forName(driver.getDriver());
                        Backend backend = new Backend();
                        BeanUtil.copyProperties(model, backend, "drivers");
                        backend.setModel(func.getModel());
                        backend.setBackend(func.getBackend());
                        backend.setStream(func.getStream());
                        backend.setPriority(func.getPriority());
                        backend.setModel(func.getModel());
                        register(clazz, backend);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        });
    };

    public void register(String key, T adapter) {
        T tempAdapter = aiMap.putIfAbsent(key, adapter);
        if (tempAdapter != null) {
            log.error("Adapter {} name {} is already exists!!", adapter.getClass().getName(), key);
        }
    }

    private void register(Class<?> clazz,  Backend backend) {
        Constructor<?> constructor = null;
        T adapter = null;
        try {
            adapter = (T) clazz.newInstance();
        } catch (Exception e) {
            try {
                constructor = clazz.getConstructor(Backend.class);
                adapter = (T)constructor.newInstance(backend);
            } catch (Exception error) {
                log.error(error.getMessage());
            }
        }
        if(adapter != null) {
            if(adapter instanceof ModelService) {
                ModelService modelService = (ModelService) adapter;
                BeanUtil.copyProperties(backend, modelService, "drivers");
            }
            register(backend.getModel(), adapter);
        }
    }

    public T getAdapter(String key) {
        return aiMap.get(key);
    }

    public T getAdapter() {
        return aiMap.values().iterator().next();
    }

    public List<T> getAdapters() {
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
