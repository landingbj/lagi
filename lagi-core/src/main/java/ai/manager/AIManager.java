package ai.manager;

import ai.common.ModelService;
import ai.common.pojo.Backend;
import ai.common.pojo.Driver;
import ai.oss.UniversalOSS;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AIManager<T> {


    private final Logger log = LoggerFactory.getLogger(AIManager.class);

    private final Map<String, T> aiMap = new ConcurrentHashMap<>();

//    public void register(List<Backend> models, List<Backend> functions) {
//        if(functions == null) {
//            return;
//        }
//        models.forEach(model->{
//            if(model.getModel() != null && model.getDriver() != null) {
//                Driver driver = Driver.builder().model(model.getModel()).driver(model.getDriver()).oss(model.getOss()).build();
//                model.setDrivers(Lists.newArrayList(driver));
//            }
//            if(model.getDriver() != null) {
//                model.getDrivers().forEach(driver -> {
//                    if(driver.getOss() == null && model.getOss() != null) {
//                        driver.setOss(model.getOss());
//                    }
//                });
//            }
//        });
//        Map<String, Backend> modelMap = models.stream().collect(Collectors.toMap(Backend::getName, model -> model));
//        functions.stream().filter(Backend::getEnable).forEach(func->{
//            Backend model =  modelMap.get(func.getBackend());
//            if(model == null) {
//                log.error("model {} not exist", func.getBackend());
//                return ;
//            }
//            for (Driver driver : model.getDrivers()) {
//                Set<String> modelSet = Arrays.stream(driver.getModel().split(",")).map(String::trim).collect(Collectors.toSet());
//                if(modelSet.contains(func.getModel())) {
//                    try {
//                        T adapter = createAdapter(driver.getDriver());
//                        Backend backend = complex(model, func, driver);
//                        propertyInjection(adapter, backend);
//                        register(backend.getModel(), adapter);
//                    } catch (Exception e) {
//                        log.error(e.getMessage());
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        });
//    };

//    private Backend complex(Backend model, Backend func, Driver driver) {
//        Backend backend = new Backend();
//        BeanUtil.copyProperties(model, backend, "drivers");
//        CopyOptions copyOption = CopyOptions.create(null, true);
//        BeanUtil.copyProperties(driver, backend, copyOption);
//        backend.setModel(func.getModel());
//        backend.setBackend(func.getBackend());
//        backend.setStream(func.getStream());
//        backend.setPriority(func.getPriority());
//        backend.setModel(func.getModel());
//        backend.setOss(driver.getOss());
//        backend.setOthers(func.getOthers());
//        backend.setDeployment(func.getDeployment());
//        backend.setApiVersion(func.getApiVersion());
//        return backend;
//    }
//
//    private T createAdapter(String driver) {
//        T adapter = null;
//        Class<?> clazz = null;
//        try {
//            clazz = Class.forName(driver);
//        } catch (Exception e) {
//            log.error( "class {} not fount {}", driver,  e.getMessage());
//        }
//        if(clazz == null) {
//            return null;
//        }
//        try {
//            adapter = (T) clazz.newInstance();
//        } catch (Exception e) {
//            log.error( "driver {} newinstance failed  {}", driver,  e.getMessage());
//        }
//        return adapter;
//    }
//
//    private void propertyInjection(T adapter, Backend backend) {
//        if(adapter instanceof ModelService) {
//            ModelService modelService = (ModelService) adapter;
//            BeanUtil.copyProperties(backend, modelService, "drivers");
//        }
//
//        if(backend.getOss() !=null) {
//            try {
//                Field universalOSS = adapter.getClass().getDeclaredField("universalOSS");
//                if(universalOSS.getType() == UniversalOSS.class) {
//                    universalOSS.setAccessible(true);
//                    universalOSS.set(adapter, OSSManager.getInstance().getOss(backend.getOss()));
//                }
//            } catch (Exception e) {
//                log.error("oss inject failed {}", e.getMessage());
//            }
//        }
//    }

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
