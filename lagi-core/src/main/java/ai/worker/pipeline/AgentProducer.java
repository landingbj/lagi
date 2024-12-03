package ai.worker.pipeline;

import ai.agent.Agent;
import ai.mr.pipeline.Producer;
import org.apache.hadoop.util.Lists;

import java.io.Serializable;
import java.util.Collection;

public class AgentProducer <R extends Serializable> implements Producer<R>{

    private final Agent<?, R> agent;

    public AgentProducer(Agent<?, R> agent) {
        this.agent = agent;
    }

    @Override
    public void init() {

    }

    @Override
    public Collection<R> produce() throws Exception {
        return Lists.newArrayList(agent.receive());
    }

    @Override
    public void cleanup() {

    }
}
