package ai.medusa.utils;

import ai.common.pojo.IndexSearchData;
import ai.common.pojo.QaPair;
import ai.common.utils.ThreadPoolManager;
import ai.llm.pojo.GetRagContext;
import ai.llm.service.CompletionsService;
import ai.llm.utils.CompletionUtil;
import ai.medusa.impl.CompletionCache;
import ai.medusa.impl.QaCache;
import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.*;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorDbService;
import ai.vector.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class PromptCacheTrigger {
    private static final Logger log = LoggerFactory.getLogger(PromptCacheTrigger.class);
    private final CompletionsService completionsService = new CompletionsService();
    private static final int SUBSTRING_THRESHOLD = PromptCacheConfig.SUBSTRING_THRESHOLD;
    private static final double LCS_RATIO_QUESTION = PromptCacheConfig.LCS_RATIO_QUESTION;
    private final CompletionCache completionCache;
    private static final ExecutorService executorService;
    private final VectorDbService vectorStoreService = new VectorDbService();
    private final LRUCache<PromptInput, List<ChatCompletionResult>> promptCache;
    private final QaCache qaCache;

    private static final LRUCache<List<ChatMessage>, String> rawAnswerCache;

    static {
        rawAnswerCache = new LRUCache<>(PromptCacheConfig.RAW_ANSWER_CACHE_SIZE);
        ThreadPoolManager.registerExecutor("medusa");
        executorService = ThreadPoolManager.getExecutor("medusa");
    }

    public PromptCacheTrigger(CompletionCache completionCache) {
        this.completionCache = completionCache;
        this.promptCache = completionCache.getPromptCache();
        this.qaCache = completionCache.getQaCache();
    }

    public PromptCacheTrigger() {
        completionCache = null;
        promptCache = null;
        qaCache = null;
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


        chatCompletionResult = completionsWithContext(promptInputWithBoundaries);
        if (chatCompletionResult == null) {
            return;
        }

        PromptInput lastPromptInput = PromptInputUtil.getLastPromptInput(promptInputWithBoundaries);
        List<ChatCompletionResult> completionResultList = promptCache.get(lastPromptInput);
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
        String firstPrompt = PromptInputUtil.getFirstPrompt(promptInputWithBoundaries);
        List<PromptInput> promptInputList = qaCache.get(firstPrompt);
//        int index = promptInputList.indexOf(lastPromptInput);
//        PromptInput promptInputInCache = promptInputList.get(index);
        PromptInput promptInputInCache = promptInputList.get(0);
        List<ChatCompletionResult> completionResults = promptCache.get(promptInputInCache);
        completionResults.add(chatCompletionResult);
        promptCache.remove(promptInputInCache);
        promptInputInCache.getPromptList().add(newestPrompt);
        List<PromptInput> promptInputs = qaCache.get(newestPrompt);
        if(promptInputs == null) {
            promptInputs = new ArrayList<>();
        }
        promptInputs.add(promptInputInCache);
//        qaCache.put(newestPrompt, promptInputList);
        qaCache.put(newestPrompt, promptInputs);
        promptCache.put(promptInputInCache, completionResults);
    }

    private synchronized void putCache(String newestPrompt, PromptInput promptInputWithBoundaries, ChatCompletionResult chatCompletionResult) {
        List<PromptInput> promptInputList = qaCache.get(newestPrompt);

        PromptInput lastPromptInput = PromptInputUtil.getLastPromptInput(promptInputWithBoundaries);
        List<ChatCompletionResult> completionResults = promptCache.get(lastPromptInput);
        if (promptInputWithBoundaries.getPromptList().size() == 1 && completionResults == null) {
           completionResults = promptCache.get(promptInputWithBoundaries);
        } else {
            putCache(promptInputWithBoundaries, lastPromptInput, chatCompletionResult, newestPrompt);
            return;
        }

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
//        String pattern = "[ \n\\.,;!\\?，。；！？#\\*：:-]";
        List<String> answerList = getRawAnswer(questionList);
//        questionList = questionList.stream().map(q->q.replaceAll(pattern, "")).collect(Collectors.toList());
//        answerList = answerList.stream().map(q->{
//            String s = q.replaceAll(pattern, "");
//            return s.substring(0, PromptCacheConfig.TRUNCATE_LENGTH);
//        }).collect(Collectors.toList());
//        int startIndex = 0;
//        String startQ = questionList.get(startIndex);
//        String startA = answerList.get(startIndex);
//        Set<String> startCoreSet = LCS.findLongestCommonSubstrings(startQ, startA, PromptCacheConfig.START_CORE_THRESHOLD);
//        Set<String> continuousSet = null;
//        for (int i = 1; i < questionList.size(); i++) {
//            String curQ = questionList.get(i);
//            String curA = answerList.get(i);
//            String lastA = answerList.get(i-1);
//            Set<String> sameCoreSet = LCS.findLongestCommonSubstrings(startQ, curA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
//            if(continuousSet == null) {
//                continuousSet = LCS.findLongestCommonSubstrings(curA, lastA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
//                Set<String> finalStartCoreSet = startCoreSet;
//                continuousSet = continuousSet.stream().filter(s->{
//                    long count = finalStartCoreSet.stream().filter(c -> c.contains(s)).count();
//                    return count > 0;
//                }).collect(Collectors.toSet());
//            } else {
//                Set<String> aaSet = LCS.findLongestCommonSubstrings(curA, lastA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
//                continuousSet = continuousSet.stream().filter(s->{
//                    long count = aaSet.stream().filter(c -> c.contains(s)).count();
//                    return count > 0;
//                }).collect(Collectors.toSet());
//            }
//            double sameRadio = LCS.getLcsRatio(startQ, sameCoreSet);
//            double conRadio = LCS.getLcsRatio(curA, continuousSet);
//            double radio = (double) PromptCacheConfig.ANSWER_CORE_THRESHOLD / curA.length();
//            if( sameRadio < LCS_RATIO_QUESTION && conRadio < radio) {
//                startIndex = i;
//                startQ = questionList.get(startIndex);
//                startA = answerList.get(startIndex);
//                startCoreSet = LCS.findLongestCommonSubstrings(startQ, startA, PromptCacheConfig.START_CORE_THRESHOLD);
//                continuousSet = null;
//            }
//        }
        List<QaPair> qaPairs = convert2QaPair(questionList, answerList);
        List<List<QaPair>> splitQaPairs = splitQaPairBySemantics(qaPairs);
        int startIndex = 0;
        if(!splitQaPairs.isEmpty() && !splitQaPairs.get(splitQaPairs.size() -1).isEmpty()) {
            startIndex = splitQaPairs.get(splitQaPairs.size() -1).get(0).getQIndex();
        }
        return PromptInput.builder()
                .parameter(promptInput.getParameter())
                .promptList(promptInput.getPromptList().subList(startIndex, promptInput.getPromptList().size()))
                .build();
    }



    public static List<Integer> analyzeChatBoundariesForIntent(ChatCompletionRequest chatCompletionRequest) {
        List<ChatMessage> chatMessages = chatCompletionRequest.getMessages();
        int finalIndex = chatMessages.size() - 1;
        LinkedList<Integer> res = new LinkedList<>();
        if(chatMessages.size() < 2) {
            res.add(finalIndex);
            return res;
        }
        List<QaPair> qaPairs = convert2QaPair(chatMessages, 30);
        List<List<QaPair>> splitQaPairs = splitQaPairBySemantics(qaPairs);
        if(!splitQaPairs.isEmpty() && !splitQaPairs.get(splitQaPairs.size() -1).isEmpty()) {
            int lastQIndex = splitQaPairs.get(splitQaPairs.size() -1).get(0).getQIndex();
            res.add(lastQIndex);
        }
        res.add(finalIndex);
        return res;
    }

    private static List<QaPair> convert2QaPair(List<String> questionList, List<String> answerList) {
        List<QaPair> qaPairs = new ArrayList<>();
        for (int i = 0; i < questionList.size(); i++) {
            String aa = answerList.get(i).trim();
            aa = StrFilterUtil.filterPunctuations(aa);
            int min = Math.min(aa.length(), 50);
            aa = aa.substring(0, min);
            String qq = questionList.get(i).trim();
            qq = StrFilterUtil.filterPunctuations(qq);
            QaPair qa = QaPair.builder().a(aa).aIndex(i).q(qq).qIndex(i).build();
            qaPairs.add(qa);
        }
        return qaPairs;
    }

    private static List<QaPair> convert2QaPair(List<ChatMessage> chatMessages, int deep) {
        List<QaPair> qaPairs = new ArrayList<>();
        for (int i = chatMessages.size() - 2, count = 0; i > 0 && count < deep; i -= 2, count++) {
            int aIndex = i;
            int qIndex = i - 1;
            ChatMessage a = chatMessages.get(aIndex);
            ChatMessage q = chatMessages.get(qIndex);
            String aa = a.getContent().trim();
            aa = StrFilterUtil.filterPunctuations(aa);
            int min = Math.min(aa.length(), 50);
            aa = aa.substring(0, min);
            String qq = q.getContent().trim();
            qq = StrFilterUtil.filterPunctuations(qq);
            QaPair qa = QaPair.builder().a(aa).aIndex(aIndex).q(qq).qIndex(qIndex).build();
            qaPairs.add(qa);
        }
        Collections.reverse(qaPairs);
        return qaPairs;
    }

    private static List<List<QaPair>> splitQaPairBySemantics(List<QaPair> qaPairs) {
        Set<String> qaCore = new HashSet<>();
        List<List<QaPair>> dialogPairs = new ArrayList<>();
        List<QaPair> curDialog = new ArrayList<>();
        dialogPairs.add(curDialog);
        for (int i = 0; i < qaPairs.size(); i++) {
            QaPair qaPair = qaPairs.get(i);
            if(StoppingWordUtil.containsStoppingWorlds(qaPair.getQ())) {
                if(curDialog.isEmpty()) {
                    curDialog.add(qaPair);
                } else {
                    curDialog = new ArrayList<>();
                    curDialog.add(qaPair);
                    dialogPairs.add(curDialog);
                }
                qaCore = LCS.findLongestCommonSubstrings(qaPair.getQ(), qaPair.getA(), PromptCacheConfig.START_CORE_THRESHOLD);
                continue;
            }
            if(ContinueWordUtil.containsStoppingWorlds(qaPair.getQ())) {
                curDialog.add(qaPair);
                continue;
            }
            if(curDialog.isEmpty()) {
                curDialog.add(qaPair);
                qaCore = LCS.findLongestCommonSubstrings(qaPair.getQ(), qaPair.getA(), PromptCacheConfig.START_CORE_THRESHOLD);
                continue;
            }
            String lastQ = curDialog.get(0).getQ();
            String curA = qaPair.getA();
            Set<String> QAnCore = LCS.findLongestCommonSubstrings(lastQ, curA, PromptCacheConfig.ANSWER_CORE_THRESHOLD);
            Set<String> retainAll = setRetainAll(qaCore, QAnCore);
            double ratio = LCS.getLcsRatio(curA, retainAll);
            double threshold = (double) PromptCacheConfig.ANSWER_CORE_THRESHOLD /  qaPair.getA().length();
            if(ratio < threshold) {
                curDialog = new ArrayList<>();
                qaCore = LCS.findLongestCommonSubstrings(qaPair.getQ(), qaPair.getA(), PromptCacheConfig.START_CORE_THRESHOLD);
                curDialog.add(qaPair);
                dialogPairs.add(curDialog);
            } else {
                curDialog.add(qaPair);
            }
        }
        return dialogPairs;
    }

    private static  Set<String> setRetainAll(Set<String> tempCore, Set<String> core) {
        Set<String> temp = new HashSet<>();
        tempCore = tempCore.stream().map(RetainWordUtil::replace).collect(Collectors.toSet());
        core = core.stream().map(RetainWordUtil::replace).collect(Collectors.toSet());
        for(String tempCoreStr: tempCore) {
            for (String coreStr: core) {
                String longStr = tempCoreStr.length() > coreStr.length() ? tempCoreStr : coreStr;
                String shortStr = tempCoreStr.length() > coreStr.length() ? coreStr : tempCoreStr;
                if(longStr.contains(shortStr)) {
                    if(!RetainWordUtil.contains(coreStr)) {
                        temp.add(coreStr);
                    }
                }
            }
        }
        return temp;
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
        ChatCompletionRequest request = completionsService.getCompletionsRequest(
                promptInput.getParameter().getSystemPrompt(), lastPrompt, promptInput.getParameter().getCategory());
        List<IndexSearchData> indexSearchDataList = vectorStoreService.searchByContext(request);
        GetRagContext ragContext = completionsService.getRagContext(indexSearchDataList);
        String context = null;
        if(ragContext != null) {
            context = ragContext.getContext();
        }
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
//        ContextLoader.loadContext();
//        PromptInput promptInput = new PromptCacheTrigger(CompletionCache.getInstance()).analyzeChatBoundaries(null);
//        System.out.println(promptInput);
    }
}
