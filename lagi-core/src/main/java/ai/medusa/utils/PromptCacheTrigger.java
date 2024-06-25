package ai.medusa.utils;

import ai.llm.service.CompletionsService;
import ai.medusa.impl.CompletionCache;
import ai.medusa.impl.QaCache;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LRUCache;
import ai.utils.qa.ChatCompletionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PromptCacheTrigger {
    private final CompletionsService completionsService = new CompletionsService();
    private static final int SUBSTRING_THRESHOLD = PromptCacheConfig.SUBSTRING_THRESHOLD;
    private static final double LCS_RATIO_QUESTION = PromptCacheConfig.LCS_RATIO_QUESTION;
    private final CompletionCache completionCache;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(PromptCacheConfig.WRITE_CACHE_THREADS);

    private static final LRUCache<String, String> rawAnswerCache;

    static {
        rawAnswerCache = new LRUCache<>(PromptCacheConfig.RAW_ANSWER_CACHE_SIZE);
    }

    public PromptCacheTrigger(CompletionCache completionCache) {
        this.completionCache = completionCache;
    }

    public void triggerWriteCache(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        executorService.execute(() -> writeCache(promptInput, chatCompletionResult));
    }

    public void writeCache(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        LRUCache<PromptInput, List<ChatCompletionResult>> promptCache = completionCache.getPromptCache();
        QaCache qaCache = completionCache.getQaCache();

        String newestPrompt = PromptInputUtil.getNewestPrompt(promptInput);
        PromptInput promptInputWithBoundaries = analyzeChatBoundaries(promptInput);

        PromptInput lastPromptInput = PromptInputUtil.getLastPromptInput(promptInputWithBoundaries);
        List<ChatCompletionResult> completionResultList = promptCache.get(lastPromptInput);

        // If the prompt list has only one prompt, add the prompt input to the cache.
        // If the prompt list has more than one prompt and the last prompt is not in the prompt cache, add the prompt to the cache.
        if (promptInputWithBoundaries.getPromptList().size() == 1 && completionResultList == null) {
            qaCache.put(newestPrompt, Collections.singletonList(promptInputWithBoundaries));
            promptCache.put(promptInputWithBoundaries, Collections.singletonList(chatCompletionResult));
            completionCache.getPromptPool().put(PooledPrompt.builder()
                    .promptInput(promptInputWithBoundaries).status(PromptCacheConfig.POOL_INITIAL).build());
            return;
        }

        // If the completionResultList size does not match the promptInput size, return.
        if (completionResultList == null || completionResultList.size() != promptInputWithBoundaries.getPromptList().size()) {
            return;
        }

        // If the prompt list has more than one prompt and the last prompt is in the prompt cache, append the prompt to the cache.
        String lastPrompt = PromptInputUtil.getLastPrompt(promptInputWithBoundaries);
        int index = qaCache.get(lastPrompt).indexOf(lastPromptInput);
        PromptInput promptInputInCache = qaCache.get(lastPrompt).get(index);
        promptInputInCache.getPromptList().add(newestPrompt);
        promptCache.get(promptInputInCache).add(chatCompletionResult);
    }

    public PromptInput analyzeChatBoundaries(PromptInput promptInput) {
        List<String> questionList = promptInput.getPromptList();
        if (questionList.size() < 2) {
            return promptInput;
        }
        List<String> answerList = new ArrayList<>();

        for (String question : questionList) {
            String answer = getRawAnswer(question);
            answerList.add(answer);
        }

        String q0 = questionList.get(0);
        String a0 = answerList.get(0);
        Set<String> s00 = LCS.findLongestCommonSubstrings(q0, a0, SUBSTRING_THRESHOLD);

        Set<String> lastDiagSet = LCS.findLongestCommonSubstrings(questionList.get(1), a0, SUBSTRING_THRESHOLD);

        int startIndex = 0;
        for (int i = 1; i < questionList.size(); i++) {
            String qi = questionList.get(i);
            String ai = answerList.get(i);
            Set<String> si = LCS.findLongestCommonSubstrings(q0, ai, SUBSTRING_THRESHOLD);
            si.retainAll(s00);

            double ratio1 = LCS.getLcsRatio(qi, si);
            String lastA = answerList.get(i - 1);
            Set<String> diagSet = LCS.findLongestCommonSubstrings(qi, lastA, SUBSTRING_THRESHOLD);
            diagSet.retainAll(lastDiagSet);
            double ratio2 = LCS.getLcsRatio(qi, diagSet);
            lastDiagSet = diagSet;
            if (ratio1 < LCS_RATIO_QUESTION && ratio2 < LCS_RATIO_QUESTION) {
                startIndex = i;
                q0 = questionList.get(startIndex);
                a0 = answerList.get(startIndex);
                s00 = LCS.findLongestCommonSubstrings(q0, a0, SUBSTRING_THRESHOLD);
            }
        }
        return PromptInput.builder()
                .category(promptInput.getCategory())
                .temperature(promptInput.getTemperature())
                .maxTokens(promptInput.getMaxTokens())
                .promptList(questionList.subList(startIndex, questionList.size()))
                .build();
    }

    private String getRawAnswer(String question) {
        if (rawAnswerCache.containsKey(question)) {
            return rawAnswerCache.get(question);
        }
        ChatCompletionRequest request = completionsService.getCompletionsRequest(question);
        ChatCompletionResult result = completionsService.completions(request);
        String answer = ChatCompletionUtil.getFirstAnswer(result);
        rawAnswerCache.put(question, answer);
        return answer;
    }
}
