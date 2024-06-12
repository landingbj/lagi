package ai.medusa.producer;

import ai.medusa.pojo.PooledPrompt;

import java.util.Collection;
import java.util.Collections;

public class TreeDiversifyPromptProducer extends DiversifyPromptProducer {
    public TreeDiversifyPromptProducer(int limit) {
        super(limit);
    }

    @Override
    public void init() {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Collection<PooledPrompt> produce(PooledPrompt item) {
        return diversify(item);
    }

    @Override
    public void consume(PooledPrompt item) throws Exception {
        super.consume(item);
    }

    public Collection<PooledPrompt> diversify(PooledPrompt item) {
        return Collections.emptyList();
    }
}
