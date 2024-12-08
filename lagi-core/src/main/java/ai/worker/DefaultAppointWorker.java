package ai.worker;

import ai.agent.Agent;
import ai.worker.pojo.WorkData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultAppointWorker<T, R> extends Worker<T, R> {

    protected Map<String, Agent<T, R>> agentMap = new ConcurrentHashMap<>();

    public DefaultAppointWorker(List<Agent<T, R>> agentList) {
        if(agentList == null) {
            return;
        }
        for (Agent<T, R> agent : agentList){
            agentMap.put(agent.getAgentName(), agent);
        }
    }

    @Override
    public R work(WorkData<T> data) {
        return null;
    }

    @Override
    public R call(WorkData<T> data) {
        String agentId = data.getAgentId();
        Agent<T, R> trAgent = agentMap.get(agentId);
        if(trAgent != null) {
            return trAgent.communicate(data.getData());
        }
        return null;
    }

    @Override
    public void notify(WorkData<T> data) {
        String agentId = data.getAgentId();
        Agent<T, R> trAgent = agentMap.get(agentId);
        if(trAgent != null) {
            trAgent.send(data.getData());
        }
    }
}
