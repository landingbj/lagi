package ai.manager;

import ai.config.pojo.WorkerConfig;
import ai.worker.RouteWorker;
import ai.worker.Worker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WorkerManager {
    @Getter
    private static WorkerManager instance = new WorkerManager();
    private WorkerManager() {}

    private static final Map<String, RouteWorker> workerMaps = new ConcurrentHashMap<>();
    private static final Map<String, Worker<?, ?>> otherWorker = new ConcurrentHashMap<>();

    public void register(List<WorkerConfig> workerConfigs) {
        if(workerConfigs == null || workerConfigs.isEmpty()) {
            return;
        }
        workerConfigs.forEach(conf -> {
            String name = conf.getName();
            String worker = conf.getWorker();
            try {
                Class<?> clazz = Class.forName(worker);
                if(RouteWorker.class.isAssignableFrom(clazz)) {
                    Constructor<?> constructor = clazz.getConstructor(WorkerConfig.class);
                    Object o = constructor.newInstance(conf);
                    workerMaps.put(name, (RouteWorker) o);
                } else {
                    Constructor<?> constructor = clazz.getConstructor(WorkerConfig.class);
                    Object o = constructor.newInstance(conf);
                    otherWorker.put(name, (Worker<?, ?>) o);
                }
            } catch (Exception e) {
            }
            }
        );
    }

    public RouteWorker getRouterWorker(String key) {
        return workerMaps.get(key);
    }

    public Worker<?, ?> getWorker(String key) {
        return otherWorker.get(key);
    }

}
