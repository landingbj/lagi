package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.medusa.exception.FailedDiversifyPromptException;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.utils.PromptCacheConfig;
import ai.utils.LagiGlobal;
import ai.vector.VectorCacheLoader;
import ai.vector.VectorStoreService;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PageDiversifyPromptProducer extends DiversifyPromptProducer {

    private static final Logger log = LoggerFactory.getLogger(PageDiversifyPromptProducer.class);
    private final int nearNum = 8;

    VectorStoreService vectorStoreService = new VectorStoreService();

    public PageDiversifyPromptProducer(int limit) {
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
        Collection<PooledPrompt> result = new ArrayList<>();
        PromptInput promptInput = item.getPromptInput();
        if(promptInput == null || promptInput.getPromptList() == null || promptInput.getPromptList().isEmpty()) {
            return result;
        }
        String prompt = promptInput.getPromptList().get(promptInput.getPromptList().size() - 1);
        String answer = VectorCacheLoader.get2L2(prompt);
        if(StrUtil.isBlank(answer)) {
            return result;
        }
        List<String> questions = VectorCacheLoader.getFromL2Near(prompt, nearNum);
        for (int i = 0 ; i < questions.size(); i++) {
            List<IndexSearchData> indexSearchData = null;
            try {
                semaphore.acquire();
                try {
                    indexSearchData = vectorStoreService.search(questions.get(i), LagiGlobal.getDefaultCategory());
                }catch (Exception e) {
                    log.error("page diversify Failed to search for question: {}", questions.get(i), e);
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException e) {
                log.error("page diversify Failed to acquire semaphore", e);
            }
            if(indexSearchData == null) {
                continue;
            }
            String text = questions.get(i);
            List<String> promptList = new ArrayList<>();
            promptList.add(text);
            PromptInput diversifiedPromptInput = PromptInput.builder()
                    .parameter(promptInput.getParameter())
                    .promptList(promptList)
                    .build();
            PooledPrompt pooledPrompt = PooledPrompt.builder()
                    .promptInput(diversifiedPromptInput)
                    .status(PromptCacheConfig.POOL_INITIAL)
                    .indexSearchData(indexSearchData)
                    .build();
            result.add(pooledPrompt);
        }
        return result;
    }

}
