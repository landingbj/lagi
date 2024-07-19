package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.medusa.exception.FailedDiversifyPromptException;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.utils.PromptCacheConfig;
import ai.vector.VectorCacheLoader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PageDiversifyPromptProducer extends DiversifyPromptProducer {

    private final int nearNum = 16;

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
        List<IndexSearchData> indexSearchDataList = VectorCacheLoader.getFromL2Near(prompt, nearNum);
        for (int i = 0 ; i < indexSearchDataList.size(); i++) {
            IndexSearchData indexSearchData = indexSearchDataList.get(i);
            String text = indexSearchData.getText().split("[?？]")[0];
            System.out.println(text);
            List<String> promptList = new ArrayList<>();
//            promptList.add(promptInput.getPromptList().get(promptInput.getPromptList().size() - 1));
            promptList.add(text);
            PromptInput diversifiedPromptInput = PromptInput.builder()
                    .parameter(promptInput.getParameter())
                    .promptList(promptList)
                    .build();
            PooledPrompt pooledPrompt = PooledPrompt.builder()
                    .promptInput(diversifiedPromptInput)
                    .status(PromptCacheConfig.POOL_INITIAL)
//                    .indexSearchData(searchByContext(diversifiedPromptInput))
                    .indexSearchData(Lists.newArrayList(indexSearchData))
                    .build();
            result.add(pooledPrompt);
            System.out.println(i + ": " + JSONUtil.toJsonStr(pooledPrompt));
        }
        return result;
    }

}
