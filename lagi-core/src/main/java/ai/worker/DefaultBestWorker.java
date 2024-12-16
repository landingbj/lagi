package ai.worker;

import ai.agent.Agent;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.workflow.container.AgentContainer;
import ai.workflow.reducer.AgentReducer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultBestWorker<T, R> extends Worker<T, R>{

    protected List<Agent<T, R>> agents;

    public DefaultBestWorker(List<Agent<T, R>> agents) {
        this.agents = agents;
    }

    protected List<IMapper> convert2Mapper(List<Agent<T, R>> agents) {
        return Collections.emptyList();
    }

    protected List<Agent<T, R>> filterAgentsBySkillMap(List<Agent<T, R>> agents, T data) {
        return agents;
    }


    @Override
    public  R work(T data){
        R result = null;
        Map<String, Object> params = new HashMap<>();
        params.put(WorkerGlobal.MAPPER_CHAT_REQUEST, data);
        List<Agent<T, R>> filtered = filterAgentsBySkillMap(agents, data);
        try (IRContainer contain = new AgentContainer()) {
            for (IMapper mapper : convert2Mapper(filtered)) {
                mapper.setParameters(params);
                mapper.setPriority(WorkerGlobal.MAPPER_PRIORITY);
                contain.registerMapper(mapper);
            }
            IReducer agentReducer = new AgentReducer();
            contain.registerReducer(agentReducer);
            @SuppressWarnings("unchecked")
            List<R> resultMatrix = (List<R>) contain.Init().running();
            if (resultMatrix.get(0) != null) {
                result = resultMatrix.get(0);
                System.out.println("DefaultBestWorker.process: result = " + result);
            }
        }
        return result;
    }

    @Override
    public  R call(T data){
        return null;
    }

    @Override
    public void notify(T data){
    }
}
