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
import java.util.Collections;
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
        if (item.getPromptInput().getMedusaMetadata().getReasoningContent() != null) {
            return Collections.emptyList();
        }
        try {
            return diversify(item);
        } catch (Exception e) {
//            throw new FailedDiversifyPromptException(item, e);
            log.error("Failed to diversify prompt: {}", item, e);
            return Collections.emptyList();
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
            return Collections.emptyList();
        }
        List<String> promptList1 = promptInput.getPromptList();
        for (String prompt : promptList1) {
            String answer = VectorCacheLoader.get2L2(prompt);
            if(StrUtil.isBlank(answer)) {
                continue;
            }
            List<String> questions = VectorCacheLoader.getFromL2Near(prompt, nearNum);
            for (int i = 0 ; i < questions.size(); i++) {
                List<IndexSearchData> indexSearchData = null;
                indexSearchData = vectorStoreService.search(questions.get(i), LagiGlobal.getDefaultCategory());
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
        }
        log.info("page diversify prompt: {}", result);
        return result;
    }

}
