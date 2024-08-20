package ai.medusa.producer;

import ai.llm.service.CompletionsService;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.exception.FailedDiversifyPromptException;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LlmDiversifyPromptProducer extends DiversifyPromptProducer {
    private final CompletionsService completionsService = new CompletionsService();

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
        ChatCompletionResult chatCompletionResult = completionsService.completions(getDiversifyRequest(item));
        PromptInput promptInput = item.getPromptInput();
        for (int i = 0; i < chatCompletionResult.getChoices().size(); i++) {
            ChatMessage message = chatCompletionResult.getChoices().get(i).getMessage();
            List<String> promptList = new ArrayList<>();
//            promptList.add(promptInput.getPromptList().get(promptInput.getPromptList().size() - 1));
            promptList.add(message.getContent());
            PromptInput diversifiedPromptInput = PromptInput.builder()
                    .parameter(promptInput.getParameter())
                    .promptList(promptList)
                    .build();
            PooledPrompt pooledPrompt = PooledPrompt.builder()
                    .promptInput(diversifiedPromptInput)
                    .status(PromptCacheConfig.POOL_INITIAL)
                    .indexSearchData(searchByContext(diversifiedPromptInput))
                    .build();
            result.add(pooledPrompt);
        }
        return result;
    }

    private ChatCompletionRequest getDiversifyRequest(PooledPrompt item) {
        PromptInput promptInput = item.getPromptInput();
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(promptInput.getParameter().getTemperature());
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(promptInput.getParameter().getMaxTokens());
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(PromptCacheConfig.DIVERSIFY_PROMPT + promptInput.getPromptList().get(promptInput.getPromptList().size() - 1));
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }
}
