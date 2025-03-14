package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.exception.FailedDiversifyPromptException;
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
    public Collection<PooledPrompt> produce(PooledPrompt item) throws FailedDiversifyPromptException {
        try {
            return diversify(item);
        } catch (Exception e) {
            throw new FailedDiversifyPromptException(item, e);
        }
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
        String question = promptInput.getPromptList().get(promptInput.getPromptList().size() - 1);

        List<IndexSearchData> indexSearchDataList = new ArrayList<>();
        if (promptInput.getParameter().getCategory() != null) {
            indexSearchDataList = search(question, promptInput.getParameter().getCategory());
        }

        if (indexSearchDataList.isEmpty()) {
            return result;
        }

        String parentId = indexSearchDataList.get(0).getId();
        IndexSearchData childIndex = getChildIndex(parentId, promptInput.getParameter().getCategory());

        if (childIndex == null) {
            return result;
        }

        String chunk = childIndex.getText();
        List<IndexSearchData> qaList = search(chunk, promptInput.getParameter().getCategory());

        if (qaList.size() <= 1) {
            return result;
        }
        qaList.forEach(qa -> {
            if (qa.getParentId() == null) {
                return;
            }
            IndexSearchData parentIndex = getParentIndex(qa.getParentId(), promptInput.getParameter().getCategory());
            List<String> promptList = new ArrayList<>();
            promptList.add(parentIndex.getText());
            PromptInput newPromptInput = PromptInput.builder()
                    .parameter(promptInput.getParameter())
                    .promptList(promptList)
                    .build();
            result.add(PooledPrompt.builder()
                    .promptInput(newPromptInput)
                    .status(PromptCacheConfig.POOL_INITIAL)
                    .indexSearchData(searchByContext(newPromptInput))
                    .build());
        });

        return result;
    }
}
