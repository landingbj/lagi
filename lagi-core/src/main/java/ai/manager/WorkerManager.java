package ai.manager;

import ai.agent.Agent;
import ai.config.pojo.WorkerConfig;
import ai.worker.Worker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class WorkerManager {
    @Getter
    private static WorkerManager instance = new WorkerManager();
    private WorkerManager() {}

    private static final Map<String, Worker<?, ?>> workerMaps = new ConcurrentHashMap<>();

    public void register(List<WorkerConfig> workerConfigs) {
        if(workerConfigs == null || workerConfigs.isEmpty()) {
            return;
        }
        workerConfigs.forEach(conf -> {
            String name = conf.getName();
            String worker = conf.getWorker();
            if(conf.getAgents() == null) {
                return;
            }
            List<String> agentNames = Arrays.stream(conf.getAgents().split(",")).map(String::trim).collect(Collectors.toList());
            try {
                Class<?> clazz = Class.forName(worker);
                Constructor<?> constructor = clazz.getConstructor(List.class);
                List<? extends Agent<?, ?>> agents = agentNames.stream().map(agentName -> AgentManager.getInstance().get(agentName)).filter(Objects::nonNull).collect(Collectors.toList());
                Object o = constructor.newInstance(agents);
                workerMaps.put(name, (Worker<?, ?>) o);
            } catch (Exception e) {
                log.error("worker {} register error {}", name, e);
            }
            }
        );
    }

    public Worker<?, ?> get(String key) {
        return workerMaps.get(key);
    }

    public <T> List<T>  getByClass(Class<T> clazz) {
        return workerMaps.values().stream().filter(worker -> worker.getClass().equals(clazz)).map( worker -> (T) worker).collect(Collectors.toList());
    }

}
