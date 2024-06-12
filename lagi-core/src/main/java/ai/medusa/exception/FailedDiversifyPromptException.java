package ai.medusa.exception;

import ai.medusa.pojo.PooledPrompt;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public class FailedDiversifyPromptException extends ExecutionException implements Serializable {
    private static final long serialVersionUID = 8631849925714762311L;
    private final PooledPrompt pooledPrompt;

    public FailedDiversifyPromptException( PooledPrompt pooledPrompt,Throwable cause) {
        super(cause);
        this.pooledPrompt = pooledPrompt;
    }

    public PooledPrompt getPooledPrompt() {
        return pooledPrompt;
    }
}
