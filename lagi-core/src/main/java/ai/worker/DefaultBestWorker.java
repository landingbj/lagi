package ai.worker;

import ai.agent.Agent;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.workflow.container.AgentContainer;
import ai.workflow.reducer.AgentReducer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultBestWorker<T, R> extends Worker<T, R>{

    protected List<IMapper> mappers;

    public DefaultBestWorker(List<Agent<T, R>> agents) {
    }

    @Override
    public  R work(T data){
        R result = null;
        Map<String, Object> params = new HashMap<>();
        params.put(WorkerGlobal.MAPPER_CHAT_REQUEST, data);
        try (IRContainer contain = new AgentContainer()) {
            for (IMapper mapper : this.mappers) {
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
