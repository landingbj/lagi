package ai.medusa.producer;

import ai.medusa.pojo.PooledPrompt;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class DirectPromptProducer extends DiversifyPromptProducer {

    public DirectPromptProducer(int limit) {
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
        if (item.getPromptInput().getMedusaMetadata().getReasoningContent() != null ||
                !item.getPromptInput().getMedusaMetadata().isCacheHit()) {
            return Collections.emptyList();
        }
        Collection<PooledPrompt> result = new ArrayList<>();
        result.add(item);
        return result;
    }

    @Override
    public void consume(PooledPrompt item) throws Exception {
        super.consume(item);
    }
}
