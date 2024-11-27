package ai.manager;


import ai.bigdata.IBigdata;
import ai.config.pojo.BigdataConfig;
import cn.hutool.core.bean.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BigdataManager {
    private final Logger log = LoggerFactory.getLogger(BigdataManager.class);

    private final Map<String, IBigdata> bigdataMap = new ConcurrentHashMap<>();

    private BigdataManager() {
    }

    private static final BigdataManager INSTANCE = new BigdataManager();

    public static BigdataManager getInstance() {
        return INSTANCE;
    }

    public void register(List<BigdataConfig> bigdataConfigs) {
        if (bigdataConfigs == null || bigdataConfigs.isEmpty()) {
            return;
        }
        bigdataConfigs.forEach(bigdataConfig -> {
            if (Boolean.FALSE.equals(bigdataConfig.getEnable())) {
                return;
            }
            Class<?> clazz;
            try {
                clazz = Class.forName(bigdataConfig.getDriver());
                Constructor<?> constructor = clazz.getConstructor(BigdataConfig.class);
                IBigdata bigdata = (IBigdata) constructor.newInstance(bigdataConfig);
                BeanUtil.copyProperties(bigdataConfig, bigdata);
                IBigdata temp = bigdataMap.putIfAbsent(bigdataConfig.getName(), bigdata);
                if (temp != null) {
                    log.error("oss {} name {} is already exists!!", bigdata.getClass().getName(), bigdataConfig.getName());
                }
            } catch (Exception e) {
                log.error("oss {} name {} register failed error : {}", bigdataConfig.getDriver(), bigdataConfig.getName(), e.getMessage());
            }
        });
    }

    public IBigdata getBigdata(String name) {
        return bigdataMap.getOrDefault(name, null);
    }

    public IBigdata getBigdata() {
        if(bigdataMap.isEmpty()) {
            return null;
        }
        Iterator<IBigdata> iterator = bigdataMap.values().iterator();
        return iterator.hasNext() ? bigdataMap.values().iterator().next() : null;
//        return bigdataMap.values().iterator().next();
    }
}
