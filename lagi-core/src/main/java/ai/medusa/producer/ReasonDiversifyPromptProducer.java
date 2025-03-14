package ai.medusa.producer;

import ai.llm.service.CompletionsService;
import ai.medusa.exception.FailedDiversifyPromptException;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.pojo.DiversifyQuestions;
import ai.medusa.utils.PromptCacheConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.JsonExtractor;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReasonDiversifyPromptProducer extends DiversifyPromptProducer {
    private static final Logger logger = LoggerFactory.getLogger(ReasonDiversifyPromptProducer.class);
    private final CompletionsService completionsService = new CompletionsService();
    private static final Pattern THINK_TAG_PATTERN = Pattern.compile("(.*?)</think>", Pattern.DOTALL);
    private final Gson gson = new Gson();

    public ReasonDiversifyPromptProducer(int limit) {
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
        ChatCompletionResult reasonCompletionResult = completionsService.completions(getReasonRequest(item));
        String reasonContent = extractReasonContent(reasonCompletionResult);
        if (reasonContent == null || reasonContent.isEmpty()) {
            return result;
        }
        ChatCompletionResult chatCompletionResult = completionsService.completions(getDiversifyRequest(item, reasonContent));
        String returnStr = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        String diversifiedContent = JsonExtractor.extractFirstJsonString(returnStr);
        if (diversifiedContent == null || diversifiedContent.isEmpty()) {
            return result;
        }
        DiversifyQuestions reasonDiversifyQuestions = gson.fromJson(diversifiedContent, DiversifyQuestions.class);
        PromptInput promptInput = item.getPromptInput();
        for (int i = 0; i < reasonDiversifyQuestions.getQuestions().size(); i++) {
            String question = reasonDiversifyQuestions.getQuestions().get(i);
            List<String> promptList = new ArrayList<>();
            promptList.addAll(promptInput.getPromptList());
            promptList.add(question);
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
            logger.info("reason diversify prompt: {}", promptList);
        }
        return result;
    }

    private ChatCompletionRequest getDiversifyRequest(PooledPrompt item, String reasonContent) {
        PromptInput promptInput = item.getPromptInput();
        String promptTemplate = PromptCacheConfig.REASON_DIVERSIFY_PROMPT;
        String prompt = promptInput.getPromptList().get(promptInput.getPromptList().size() - 1);
        String result = String.format(promptTemplate, prompt, reasonContent);
        return getCompletionRequest(promptInput, null, result);
    }

    private ChatCompletionRequest getReasonRequest(PooledPrompt item) {
        PromptInput promptInput = item.getPromptInput();
        String prompt = promptInput.getPromptList().get(promptInput.getPromptList().size() - 1);
        return getCompletionRequest(promptInput, PromptCacheConfig.getReasonModel(), prompt);
    }

    private ChatCompletionRequest getCompletionRequest(PromptInput promptInput, String model, String prompt) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(promptInput.getParameter().getTemperature());
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(promptInput.getParameter().getMaxTokens());
        chatCompletionRequest.setModel(model);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    public String extractReasonContent(ChatCompletionResult chatCompletionResult) {
        String reasoningContent = ChatCompletionUtil.getReasoningContent(chatCompletionResult);
        String content = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        if (reasoningContent == null) {
            Matcher matcher = THINK_TAG_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).trim().replace("<think>", "");
            }
        }
        return reasoningContent;
    }
}
