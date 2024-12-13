package ai.worker;

import ai.agent.Agent;
import ai.mr.pipeline.ThreadedProducerConsumerPipeline;
import ai.worker.pipeline.AgentConsumer;
import ai.worker.pipeline.AgentProducer;

import java.io.Serializable;
import java.util.List;

public class DefaultPipelineWorker<T extends Serializable, R extends Serializable> extends Worker<T, R> {

    protected ThreadedProducerConsumerPipeline<T> pipeline;

    public DefaultPipelineWorker(List<Agent<?, T>> produceAgent, List<Agent<T, ?>> consumerAgent) {
        this.pipeline = new ThreadedProducerConsumerPipeline<>(produceAgent.size(), consumerAgent.size(), produceAgent.size() + consumerAgent.size(), produceAgent.size());
        for (Agent<?, T> agent : produceAgent) {
            AgentProducer<T> agentProducer = new AgentProducer<>(agent);
            pipeline.connectProducer(agentProducer);
        }
        for (Agent<T, ?> agent : consumerAgent) {
            AgentConsumer<T> agentConsumer =new AgentConsumer<>(agent);
            pipeline.connectConsumer(agentConsumer);
        }
    }

    @Override
    public R work(T data) {
        pipeline.start();
        return null;
    }

    @Override
    public R call(T data) {
        return null;
    }

    @Override
    public void notify(T data) {

    }
}
