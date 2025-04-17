package ai.medusa.producer;

import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.config.pojo.RAGFunction;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.service.CompletionsService;
import ai.llm.utils.PriorityLock;
import ai.medusa.pojo.DiversifyQuestions;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.exception.FailedDiversifyPromptException;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.JsonExtractor;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class LlmDiversifyPromptProducer extends DiversifyPromptProducer {
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();

    public LlmDiversifyPromptProducer(int limit) {
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
        if (item.getPromptInput().getReasoningContent() != null) {
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
        return getDiversifiedResult(item);
    }

    private Collection<PooledPrompt> getDiversifiedResult(PooledPrompt item) {
        Collection<PooledPrompt> result = new ArrayList<>();
        ChatCompletionResult chatCompletionResult = completionsService.completions(getDiversifyRequest(item));
        String returnStr = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        String diversifiedContent = JsonExtractor.extractFirstJsonString(returnStr);
        if (diversifiedContent == null || diversifiedContent.isEmpty()) {
            return result;
        }
        DiversifyQuestions diversifyQuestions = gson.fromJson(diversifiedContent, DiversifyQuestions.class);
        PromptInput promptInput = item.getPromptInput();
        int size = diversifyQuestions.getQuestions().size();
        if (size > PromptCacheConfig.LLM_DIVERSIFY_LIMIT) {
            size = PromptCacheConfig.LLM_DIVERSIFY_LIMIT;
        }
        for (int i = 0; i < size; i++) {
            String question = diversifyQuestions.getQuestions().get(i);
            List<String> promptList = new ArrayList<>();
            promptList.addAll(promptInput.getPromptList());
            promptList.add(question);
            PromptInput diversifiedPromptInput = PromptInput.builder()
                    .parameter(promptInput.getParameter())
                    .promptList(promptList)
                    .build();
            List<IndexSearchData>  indexSearchDataList = null;
            if (RAG_CONFIG.getEnable()) {
                indexSearchDataList = searchByContext(diversifiedPromptInput);
            }
            boolean needSplitBoundary = promptList.size() != 2;
            PooledPrompt pooledPrompt = PooledPrompt.builder()
                    .promptInput(diversifiedPromptInput)
                    .status(PromptCacheConfig.POOL_INITIAL)
                    .indexSearchData(indexSearchDataList)
                    .needSplitBoundary(needSplitBoundary)
                    .build();
            result.add(pooledPrompt);
        }
        log.info("llm diversify prompt: {}", result);
        return result;
    }

    private ChatCompletionRequest getDiversifyRequest(PooledPrompt item) {
        PromptInput promptInput = item.getPromptInput();
        EnhanceChatCompletionRequest chatCompletionRequest = new EnhanceChatCompletionRequest();
        chatCompletionRequest.setPriority(PriorityLock.LOW_PRIORITY);
        chatCompletionRequest.setTemperature(promptInput.getParameter().getTemperature());
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(promptInput.getParameter().getMaxTokens());
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        String promptTemplate = PromptCacheConfig.DIVERSIFY_PROMPT;
        String prompt = promptInput.getPromptList().get(promptInput.getPromptList().size() - 1);
        String content = String.format(promptTemplate, PromptCacheConfig.LLM_DIVERSIFY_LIMIT, prompt);
        message.setContent(content);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }
}
