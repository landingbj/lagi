package ai.medusa.consumer;

import ai.common.pojo.IndexSearchData;
import ai.llm.service.CompletionsService;
import ai.medusa.exception.FailedDiversifyPromptException;
import ai.medusa.impl.CompletionCache;
import ai.medusa.impl.QaCache;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.utils.PromptCacheConfig;
import ai.mr.pipeline.Consumer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompletePromptConsumer implements Consumer<PooledPrompt> {
    private final CompletionCache cache;
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();

    public CompletePromptConsumer(CompletionCache completionCache) {
        this.cache = completionCache;
    }

    @Override
    public void init() {
    }

    @Override
    public void consume(PooledPrompt item) throws FailedDiversifyPromptException {
        try {
            ChatCompletionResult result = completions(item);
            if (result != null) {
                cache.put(item.getPromptInput(), result);
            }
        } catch (Exception e) {
            throw new FailedDiversifyPromptException(item, e);
        }
    }

    @Override
    public void cleanup() {
    }

    public ChatCompletionResult completions(PooledPrompt item) {
        List<IndexSearchData> indexSearchDataList = item.getIndexSearchData();
        ChatCompletionRequest completionRequest = getCompletionsRequest(item, indexSearchDataList);
        ChatCompletionResult chatCompletionResult = completionsService.completions(completionRequest);
        if (!indexSearchDataList.isEmpty()) {
            IndexSearchData indexData = indexSearchDataList.get(0);
            List<String> imageList = getImageFiles(indexData);
            for (int i = 0; i < chatCompletionResult.getChoices().size(); i++) {
                ChatMessage outputMessage = chatCompletionResult.getChoices().get(i).getMessage();
                outputMessage.setContext(indexData.getText());
                outputMessage.setFilename(indexData.getFilename());
                outputMessage.setFilepath(indexData.getFilepath());
                outputMessage.setImageList(imageList);
            }
        }
        return chatCompletionResult;
    }

    private ChatCompletionRequest getCompletionsRequest(PooledPrompt item, List<IndexSearchData> indexSearchDataList) {
        PromptInput promptInput = item.getPromptInput();
        String prompt = promptInput.getPromptList().get(promptInput.getPromptList().size() - 1);
        ChatCompletionRequest chatCompletionRequest = completionsService.getCompletionsRequest(prompt);
        completionsService.addVectorDBContext(chatCompletionRequest, indexSearchDataList);
        return chatCompletionRequest;
    }

    private List<String> getImageFiles(IndexSearchData indexData) {
        List<String> imageList = null;
        if (indexData.getImage() != null && !indexData.getImage().isEmpty()) {
            imageList = new ArrayList<>();
            List<JsonObject> imageObjectList = gson.fromJson(indexData.getImage(), new TypeToken<List<JsonObject>>() {
            }.getType());
            for (JsonObject image : imageObjectList) {
                String url = image.get("path").getAsString();
                imageList.add(url);
            }
        }
        return imageList;
    }
}
