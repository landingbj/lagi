package ai.worker.pipeline;

import ai.agent.Agent;
import ai.mr.pipeline.Consumer;

import java.io.Serializable;

public class AgentConsumer<T extends Serializable> implements Consumer<T> {

    private final Agent<T, ?> agent;

    public AgentConsumer(Agent<T, ?> agent) {
        this.agent = agent;
    }

    @Override
    public void init() {

    }

    @Override
    public void consume(T data) throws Exception {
        agent.send(data);
    }

    @Override
    public void cleanup() {

    }
}
