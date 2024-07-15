package ai.worker;

import ai.config.pojo.AgentConfig;
import ai.config.pojo.WorkerConfig;
import ai.utils.LagiGlobal;
import ai.worker.audio.Asr4FlightsWorker;
import ai.worker.social.RobotWorker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerFactory {
    private static final List<AgentConfig> agentConfigList = LagiGlobal.getConfig().getAgents();
    private static final List<WorkerConfig> workerConfigList = LagiGlobal.getConfig().getWorkers();

    private static final Map<String, AgentConfig> agentMap = new ConcurrentHashMap<>();
    private static final Map<String, WorkerConfig> workerMap = new ConcurrentHashMap<>();

    static {
        if (agentConfigList != null && workerConfigList != null && !agentConfigList.isEmpty() && !workerConfigList.isEmpty()) {
            for (AgentConfig agent : agentConfigList) {
                agentMap.put(agent.getName(), agent);
            }
            for (WorkerConfig worker : workerConfigList) {
                workerMap.put(worker.getName(), worker);
            }
        }
    }

    public static Worker getWorker(String name) {
        WorkerConfig workerConfig = workerMap.get(name);
        if (workerConfig == null) {
            throw new RuntimeException("Worker not found");
        }
        AgentConfig agentConfig = agentMap.get(workerConfig.getAgent());

        if (workerConfig.getWorker().equals(WorkerGlobal.ROBOT_WORKER_CLASS)) {
            return new RobotWorker(agentConfig);
        } else if (workerConfig.getWorker().equals(WorkerGlobal.ASR_FLIGHT_WORKER_CLASS)) {
            return new Asr4FlightsWorker();
        } else {
            throw new RuntimeException("Worker not found");
        }
    }
}
