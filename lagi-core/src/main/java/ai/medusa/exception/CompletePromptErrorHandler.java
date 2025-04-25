package ai.medusa.exception;

import ai.dao.Pool;
import ai.medusa.pojo.PooledPrompt;
import ai.mr.pipeline.ProducerConsumerErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompletePromptErrorHandler implements ProducerConsumerErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(CompletePromptErrorHandler.class);

    private final Pool<PooledPrompt> pool;

    public CompletePromptErrorHandler(Pool<PooledPrompt> pool) {
        super();
        this.pool = pool;
    }

    @Override
    public void handle(Exception e) {
        logger.warn("Complete Prompt Exception:", e);
        if (e instanceof FailedDiversifyPromptException) {
            PooledPrompt item = ((FailedDiversifyPromptException) e).getPooledPrompt();
            this.pool.returnItem(item);
            logger.warn("return item to pool: {}", item);
        }
    }
}