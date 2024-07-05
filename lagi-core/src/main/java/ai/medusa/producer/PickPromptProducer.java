package ai.medusa.producer;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import ai.dao.Pool;
import ai.medusa.pojo.PooledPrompt;
import ai.mr.pipeline.DelayedPoolProducer;

public class PickPromptProducer extends DelayedPoolProducer<PooledPrompt, PooledPrompt> {
    public PickPromptProducer(Pool<PooledPrompt> pool) {
        super(1, TimeUnit.DAYS, 1, pool);
    }

    @Override
    public void init() {
    }

    @Override
    public void cleanup() {
    }

    @Override
    protected Collection<PooledPrompt> delayedProduce(PooledPrompt prompt) throws InterruptedException {
        return Collections.singleton(prompt);
    }
}
