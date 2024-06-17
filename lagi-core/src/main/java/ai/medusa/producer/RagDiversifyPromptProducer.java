package ai.medusa.producer;

import ai.medusa.PromptCacheConfig;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;

import java.util.ArrayList;
import java.util.Collection;

public class RagDiversifyPromptProducer extends DiversifyPromptProducer {
    public RagDiversifyPromptProducer(int limit) {
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
        return getDiversifiedResult(item);
    }

    private Collection<PooledPrompt> getDiversifiedResult(PooledPrompt item) {
        Collection<PooledPrompt> result = new ArrayList<>();

        PromptInput promptInput = item.getPromptInput();
        PromptInput diversifiedPromptInput = PromptInput.builder()
                .maxTokens(promptInput.getMaxTokens())
                .temperature(promptInput.getTemperature())
                .category(promptInput.getCategory())
                .promptList(promptInput.getPromptList())
                .build();
        PooledPrompt pooledPrompt = PooledPrompt.builder()
                .promptInput(diversifiedPromptInput)
                .status(PromptCacheConfig.POOL_INITIAL)
                .indexSearchData(searchByContext(diversifiedPromptInput))
                .build();
        result.add(pooledPrompt);
        return result;
    }
}
