package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.medusa.PromptCacheConfig;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        List<IndexSearchData> indexSearchDataList =  searchByContext(diversifiedPromptInput);
        if (indexSearchDataList.isEmpty()) {
            return result;
        }
        
        String chunk = indexSearchDataList.get(0).getText();
        List<IndexSearchData> qaList = search(chunk, promptInput.getCategory());

        if (qaList.size() <= 1) {
            return result;
        }
        qaList.forEach(qa -> {
            if (qa.getParentId() == null) {
                return;
            }
            IndexSearchData parentIndex = getParentIndex(qa.getParentId(), promptInput.getCategory());
            List<String> promptList = new ArrayList<>();
            promptList.add(parentIndex.getText());
            PromptInput newPromptInput = PromptInput.builder()
                    .maxTokens(promptInput.getMaxTokens())
                    .temperature(promptInput.getTemperature())
                    .category(promptInput.getCategory())
                    .promptList(promptList)
                    .build();
            result.add(PooledPrompt.builder()
                    .promptInput(newPromptInput).status(PromptCacheConfig.POOL_INITIAL)
                    .build());
        });

        return result;
    }
}
