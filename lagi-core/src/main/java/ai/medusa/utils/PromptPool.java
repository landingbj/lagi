package ai.medusa.utils;

import ai.dao.Pool;
import ai.medusa.pojo.PooledPrompt;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PromptPool implements Pool<PooledPrompt> {
    private final BlockingQueue<PooledPrompt> cache;

    private final PromptTransitionManager transitionManager;

    public PromptPool(int cacheSize) {
        this(cacheSize, new PromptTransitionManager());
    }

    public PromptPool(int cacheSize, PromptTransitionManager transitionManager) {
        this.transitionManager = transitionManager;
        this.cache = new LinkedBlockingQueue<>(cacheSize);
    }

    @Override
    public PooledPrompt take() {
        try {
            return cache.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PooledPrompt put(PooledPrompt item) {
        if (cache.offer(item)) {
            return item;
        }
        return null;
    }

    public void transition(PooledPrompt item) {
        transitionManager.transition(item.getStatus());
    }

    public void undoTransition(PooledPrompt item) {
        transitionManager.reverseTransition(item.getStatus());
    }

    @Override
    public void returnItem(PooledPrompt item) {
        put(item);
    }

    @Override
    public Collection<PooledPrompt> put(Collection<? extends PooledPrompt> collection) {
        return Collections.emptyList();
    }

    @Override
    public void returnItem(Collection<? extends PooledPrompt> collection) {
    }

    @Override
    public Collection<PooledPrompt> putIfAbsent(Collection<? extends PooledPrompt> collection) {
        return Collections.emptyList();
    }

    @Override
    public boolean contains(PooledPrompt s) {
        return cache.contains(s);
    }

    @Override
    public int delete(Collection<? extends PooledPrompt> collection) {
        return 0;
    }
}
