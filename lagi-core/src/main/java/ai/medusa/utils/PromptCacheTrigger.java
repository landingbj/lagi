package ai.medusa.utils;

import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.llm.service.CompletionsService;
import ai.llm.utils.CompletionUtil;
import ai.medusa.impl.CompletionCache;
import ai.medusa.impl.QaCache;
import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PromptCacheTrigger {
    private static final Logger log = LoggerFactory.getLogger(PromptCacheTrigger.class);
    private final CompletionsService completionsService = new CompletionsService();
    private static final int SUBSTRING_THRESHOLD = PromptCacheConfig.SUBSTRING_THRESHOLD;
    private static final double LCS_RATIO_QUESTION = PromptCacheConfig.LCS_RATIO_QUESTION;
    private final CompletionCache completionCache;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(PromptCacheConfig.WRITE_CACHE_THREADS);
    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final LRUCache<PromptInput, List<ChatCompletionResult>> promptCache;
    private final QaCache qaCache;

    private static final LRUCache<List<ChatMessage>, String> rawAnswerCache;

    static {
        rawAnswerCache = new LRUCache<>(PromptCacheConfig.RAW_ANSWER_CACHE_SIZE);
    }

    public PromptCacheTrigger(CompletionCache completionCache) {
        this.completionCache = completionCache;
        this.promptCache = completionCache.getPromptCache();
        this.qaCache = completionCache.getQaCache();
    }

    public void triggerWriteCache(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        executorService.execute(() -> writeCache(promptInput, chatCompletionResult));
    }

    public void writeCache(PromptInput promptInput, ChatCompletionResult chatCompletionResult) {
        String newestPrompt = PromptInputUtil.getNewestPrompt(promptInput);

        if (chatCompletionResult != null) {
            putCache(newestPrompt, promptInput, chatCompletionResult);
            return;
        }

        PromptInput promptInputWithBoundaries = analyzeChatBoundaries(promptInput);

        PromptInput lastPromptInput = PromptInputUtil.getLastPromptInput(promptInputWithBoundaries);
        List<ChatCompletionResult> completionResultList = promptCache.get(lastPromptInput);

        chatCompletionResult = completionsWithContext(promptInputWithBoundaries);
        if (chatCompletionResult == null) {
            return;
        }

        // If the prompt list has only one prompt, add the prompt input to the cache.
        // If the prompt list has more than one prompt and the last prompt is not in the prompt cache, add the prompt to the cache.
        if (promptInputWithBoundaries.getPromptList().size() == 1 && completionResultList == null) {
            putCache(newestPrompt, promptInputWithBoundaries, chatCompletionResult);
            return;
        }

        // If the completionResultList size does not match the promptInput size, return.
        if (completionResultList == null) {
            return;
        }

        // If the prompt list has more than one prompt and the last prompt is in the prompt cache, append the prompt to the cache.
        putCache(promptInputWithBoundaries, lastPromptInput, chatCompletionResult, newestPrompt);
    }

    private synchronized void putCache(PromptInput promptInputWithBoundaries, PromptInput lastPromptInput, ChatCompletionResult chatCompletionResult, String newestPrompt) {
        String lastPrompt = PromptInputUtil.getLastPrompt(promptInputWithBoundaries);
        List<PromptInput> promptInputList = qaCache.get(lastPrompt);
        int index = promptInputList.indexOf(lastPromptInput);
        PromptInput promptInputInCache = promptInputList.get(index);
        List<ChatCompletionResult> completionResults = promptCache.get(promptInputInCache);
        completionResults.add(chatCompletionResult);
        promptCache.remove(promptInputInCache);
        promptInputInCache.getPromptList().add(newestPrompt);
        qaCache.put(newestPrompt, promptInputList);
        promptCache.put(promptInputInCache, completionResults);
    }

    private synchronized void putCache(String newestPrompt, PromptInput promptInputWithBoundaries, ChatCompletionResult chatCompletionResult) {
        List<PromptInput> promptInputList = qaCache.get(newestPrompt);
        List<ChatCompletionResult> completionResults = promptCache.get(promptInputWithBoundaries);

        if (promptInputList == null || promptInputList.isEmpty()) {
            promptInputList = new ArrayList<>();
        }

        if (completionResults == null || completionResults.isEmpty()) {
            completionResults = new ArrayList<>();
        }

        promptInputList.add(promptInputWithBoundaries);
        completionResults.add(chatCompletionResult);

        qaCache.put(newestPrompt, promptInputList);
        promptCache.put(promptInputWithBoundaries, completionResults);
    }

    public PromptInput analyzeChatBoundaries(PromptInput promptInput) {
        List<String> questionList = promptInput.getPromptList();
        if (questionList.size() < 2) {
            return promptInput;
        }
        String pattern = "[ \n\\.,;!\\?，。；！？#\\*：:-]";
        List<String> answerList = getRawAnswer(questionList);
        questionList = questionList.stream().map(q->q.replaceAll(pattern, "")).collect(Collectors.toList());
        answerList = answerList.stream().map(q->{
            String s = q.replaceAll(pattern, "");
            return s.substring(0, PromptCacheConfig.TRUNCATE_LENGTH);
        }).collect(Collectors.toList());
        int startIndex = 0;
        String startQ = questionList.get(startIndex);
        String startA = answerList.get(startIndex);
        Set<String> startCoreSet = LCS.findLongestCommonSubstrings(startQ, startA, PromptCacheConfig.START_CORE_THRESHOLD);
        Set<String> continuousSet = null;
        for (int i = 1; i < questionList.size(); i++) {
            String curQ = questionList.get(i);
            String curA = answerList.get(i);
            String lastA = answerList.get(i-1);
            Set<String> sameCoreSet = LCS.findLongestCommonSubstrings(startQ, curA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
            if(continuousSet == null) {
                continuousSet = LCS.findLongestCommonSubstrings(curA, lastA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
                Set<String> finalStartCoreSet = startCoreSet;
                continuousSet = continuousSet.stream().filter(s->{
                    long count = finalStartCoreSet.stream().filter(c -> c.contains(s)).count();
                    return count > 0;
                }).collect(Collectors.toSet());
            } else {
                Set<String> aaSet = LCS.findLongestCommonSubstrings(curA, lastA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
                continuousSet = continuousSet.stream().filter(s->{
                    long count = aaSet.stream().filter(c -> c.contains(s)).count();
                    return count > 0;
                }).collect(Collectors.toSet());
            }
            double sameRadio = LCS.getLcsRatio(startQ, sameCoreSet);
            double conRadio = LCS.getLcsRatio(curA, continuousSet);
            double radio = (double) PromptCacheConfig.ANSWER_CORE_THRESHOLD / curA.length();
            if( sameRadio < LCS_RATIO_QUESTION && conRadio < radio) {
                startIndex = i;
                startQ = questionList.get(startIndex);
                startA = answerList.get(startIndex);
                startCoreSet = LCS.findLongestCommonSubstrings(startQ, startA, PromptCacheConfig.START_CORE_THRESHOLD);
                continuousSet = null;
            }
        }
        return PromptInput.builder()
                .parameter(promptInput.getParameter())
                .promptList(promptInput.getPromptList().subList(startIndex, promptInput.getPromptList().size()))
                .build();
    }


    public static int analyzeChatBoundariesForIntent(ChatCompletionRequest chatCompletionRequest) {
        int startIndex = 0;

        if (chatCompletionRequest.getMessages().size() < 3) {
            return startIndex;
        }
        List<String> contents = chatCompletionRequest.getMessages().stream().map(ChatMessage::getContent).collect(Collectors.toList());
        startIndex = getLastRelateQuestionIndex(contents.size() - 1, contents.size() - 3, contents);
        if (startIndex == contents.size() - 1) {
            return startIndex;
        }
        String curQ = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String lastQ = chatCompletionRequest.getMessages().get(startIndex).getContent();
        try {
            VectorStoreService vectorStoreService = new VectorStoreService();

            List<IndexSearchData> prompts = vectorStoreService.search(curQ, chatCompletionRequest.getCategory());
            List<IndexSearchData> complexPrompts = vectorStoreService.search(lastQ + "," + curQ, chatCompletionRequest.getCategory());

            if (!prompts.isEmpty() && !complexPrompts.isEmpty()) {
                Float distance = prompts.get(0).getDistance();
                Float distance1 = complexPrompts.get(0).getDistance();
                if (distance < distance1) {
                    return contents.size() - 1;
                }
            }
        } catch (Exception e) {
            log.error("vector query error {}", e.getMessage());
        }
        return startIndex;
    }


    public static int getLastRelateQuestionIndex(int curQuestionIndex, int lastQuestionIndex, List<String> contentList) {
        if (lastQuestionIndex < 0) {
            return curQuestionIndex;
        }
        String curQuestion = contentList.get(curQuestionIndex);
        int answerIndex = lastQuestionIndex + 1;
        String question = contentList.get(lastQuestionIndex);
        String answer = contentList.get(answerIndex);
        Set<String> qq = LCS.findLongestCommonSubstrings(curQuestion, question, 2);
        Set<String> qa = LCS.findLongestCommonSubstrings(curQuestion, answer, 2);
        double ratio1 = LCS.getLcsRatio(curQuestion, qq);
        double ratio2 = LCS.getLcsRatio(curQuestion, qa);
        if (ratio1 > 0.25d || ratio2 > 0.35d) {
            return getLastRelateQuestionIndex(lastQuestionIndex, lastQuestionIndex - 2, contentList);
        }
        return getLastRelateQuestionIndex(curQuestionIndex, lastQuestionIndex - 2, contentList);
    }

    private List<String> getRawAnswer(List<String> questionList) {
        List<ChatMessage> messages = new ArrayList<>();
        List<String> answerList = new ArrayList<>();
        for (String question : questionList) {
            messages.add(completionsService.getChatMessage(question, LagiGlobal.LLM_ROLE_USER));
            String answer = completions(new ArrayList<>(messages));
            messages.add(completionsService.getChatMessage(answer, LagiGlobal.LLM_ROLE_ASSISTANT));
            answerList.add(answer);
        }
        return answerList;
    }

    private ChatCompletionResult completionsWithContext(PromptInput promptInput) {
        String lastPrompt = PromptInputUtil.getNewestPrompt(promptInput);
        String text = lastPrompt;
        if(promptInput.getPromptList().size() > 1) {
            String firstPrompt = PromptInputUtil.getFirstPrompt(promptInput);
            text  = firstPrompt + "," +text;
        }
//        String text = String.join(";", promptInput.getPromptList());
        List<IndexSearchData> indexSearchDataList = vectorStoreService.search(text, promptInput.getParameter().getCategory());
        String context = completionsService.getRagContext(indexSearchDataList);
        ChatCompletionRequest request = completionsService.getCompletionsRequest(
                promptInput.getParameter().getSystemPrompt(), lastPrompt, promptInput.getParameter().getCategory());
        if (context != null) {
            completionsService.addVectorDBContext(request, context);
        }
        ChatCompletionResult result = completionsService.completions(request);
        CompletionUtil.populateContext(result, indexSearchDataList, context);
        return result;
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String completions(List<ChatMessage> messages) {
        String answer = rawAnswerCache.get(messages);
        if (answer == null) {
            ChatCompletionRequest request = completionsService.getCompletionsRequest(messages);
            ChatCompletionResult result = completionsService.completions(request);
            answer = ChatCompletionUtil.getFirstAnswer(result);
            rawAnswerCache.put(messages, answer);
            delay(PromptCacheConfig.getPreDelay());
        }
        return answer;
    }


    public static void main(String[] args) {
        ContextLoader.loadContext();
        PromptInput promptInput = new PromptCacheTrigger(CompletionCache.getInstance()).analyzeChatBoundaries(null);
        System.out.println(promptInput);
    }
}
