package ai.medusa.utils;

import ai.common.pojo.IndexSearchData;
import ai.llm.service.CompletionsService;
import ai.medusa.impl.CompletionCache;
import ai.medusa.impl.QaCache;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LRUCache;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorStoreService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PromptCacheTrigger {
    private final Logger log = LoggerFactory.getLogger(PromptCacheTrigger.class);
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


    public int analyzeChatBoundariesForIntent(ChatCompletionRequest chatCompletionRequest) {
        int startIndex = 0;

        if (chatCompletionRequest.getMessages().size() < 3) {
            return startIndex;
        }
        List<String> contents = chatCompletionRequest.getMessages().stream().map(ChatMessage::getContent).collect(Collectors.toList());
        startIndex = getLastRelateQuestionIndex(contents.size() - 1, contents.size() - 3, contents);
        if(startIndex == contents.size() -1) {
            return startIndex;
        }
        String curQ = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        int lastIndex = getLastRelateQuestionIndex(startIndex, startIndex - 2, contents);
        String lastQ = chatCompletionRequest.getMessages().get(lastIndex).getContent();
        try {
            VectorStoreService vectorStoreService = new VectorStoreService();
            List<IndexSearchData> prompts = vectorStoreService.search(curQ, chatCompletionRequest.getCategory());
            List<IndexSearchData> complexPrompts = vectorStoreService.search(lastQ + "," + curQ, chatCompletionRequest.getCategory());
            if(!prompts.isEmpty() && !complexPrompts.isEmpty()) {
                Float distance = prompts.get(0).getDistance();
                Float distance1 = complexPrompts.get(0).getDistance();
                if(distance1  < distance) {
                    return lastIndex;
                }
            }
        } catch (Exception e) {
            log.error("vector query error {}", e.getMessage());
        }
        return startIndex;
    }


    public static int getLastRelateQuestionIndex(int curQuestionIndex,int lastQuestionIndex,  List<String> contentList) {
        if(lastQuestionIndex < 0) {
            return curQuestionIndex;
        }
//        String curQuestion = BaseAnalysis.parse(contentList.get(curQuestionIndex)).toStringWithOutNature();
        String curQuestion = contentList.get(curQuestionIndex);
        int answerIndex = lastQuestionIndex + 1;
//        String question = BaseAnalysis.parse(contentList.get(lastQuestionIndex)).toStringWithOutNature();
        String question = contentList.get(lastQuestionIndex);
//        String answer = BaseAnalysis.parse(contentList.get(answerIndex)).toStringWithOutNature();
        String answer = contentList.get(answerIndex);
//        String[] lcsQ = ai.core.matrix.LCS.lcs(answer, curQuestion, "[ \\\\.,;!\\\\?，。；！？]", 0, false);
//        String[] lcsA = ai.core.matrix.LCS.lcs(question, curQuestion, "[ \\\\.,;!\\\\?，。；！？]", 0, false);
        Set<String> qq = LCS.findLongestCommonSubstrings(curQuestion, question, 2);
        Set<String> qa = LCS.findLongestCommonSubstrings(curQuestion, answer, 2);
        double ratio1 = LCS.getLcsRatio(curQuestion, qq);
        double ratio2 = LCS.getLcsRatio(curQuestion, qa);
//        if(lcsA.length > 0 || lcsQ.length > 0) {
        if(ratio1 > 0.25d || ratio2 > 0.35d) {
            return getLastRelateQuestionIndex(lastQuestionIndex, lastQuestionIndex - 2, contentList);
        }
        return getLastRelateQuestionIndex(curQuestionIndex, lastQuestionIndex - 2, contentList);
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

    public static void main(String[] args) {
        ArrayList<String> messages = Lists.newArrayList("我的社保卡丢失了，该怎么办？",
                "根据您提供的背景信息，如果您的社会保障卡丢失了，您需要按照以下步骤进行操作：<br><br>1. 携带您的居民身份证。<br>2. 前往社会保障卡服务网点。<br>3. 在该网点进行正式挂失手续。<br>4. 同时办理补卡手续。<br>5. 等待15个工作日。<br>6. 携带居民身份证和领卡证明返回社会保障卡服务网点领取新卡。<br><br>请注意，一旦您办理了正式挂失和补卡手续，就无法撤销挂失。",
                "如何进行补办？",
                "根据您提供的背景信息，补办丢失的社会保障卡的步骤如下：<br><br>1. **携带本人居民身份证**：首先，您需要携带自己的居民身份证，这是办理挂失和补卡手续的必要证件。<br><br>2. **前往社会保障卡服务网点**：您需要前往提供社会保障卡服务的网点。这些网点可能分布在各个社区、街道或当地的人力资源和社会保障机构。<br><br>3. **正式挂失**：在社会保障卡服务网点，您需要告知工作人员您的社会保障卡已经丢失，并提供居民身份证，以便进行正式挂失。这是必要的一步，以防止丢失的卡片被他人滥用。<br><br>4. **办理补卡手续**：在挂失的同时，您可以向工作人员说明您需要办理补卡手续。工作人员会指导您完成所需的申请和填写相关表格。<br><br>5. **等待15个工作日**：补卡手续办理完成后，您可能需要等待一段时间，通常是15个工作日，以便新的社会保障卡制作完成。<br><br>6. **领卡**：在规定的等待时间过后，您需要携带您的居民身份证和领卡证明返回社会保障卡服务网点，领取新的社会保障卡。<br><br>7. **注意挂失不可撤销**：一旦您办理了正式挂失和补卡手续，就不能撤销挂失。这意味着即使您的社会保障卡在挂失后被找到，也不能继续使用，您只能使用新补办的卡片。<br><br>以上步骤确保了社会保障卡的安全和补办流程的顺利进行。",
                "我可以进行网上办理吗？");
        int lastRelateQuestionIndex = getLastRelateQuestionIndex(messages.size() - 1, messages.size() -3, messages);
        System.out.println(lastRelateQuestionIndex);

        messages.add("根据您提供的《关于印发北京市社会保障卡使用管理暂行办法的通知》（京人社保发〔2009〕152号）中的信息，该通知并没有明确提及网上办理社会保障卡挂失和补办的具体流程。通常来说，社会保障卡的挂失和补办涉及到居民个人信息的安全，需要核实身份证明和办理手续，因此很可能需要本人亲自到服务网点进行办理。<br><br>但是，随着技术的发展和便民服务的改进，一些地区可能已经推出了网上办理的服务。建议您登录当地人力资源和社会保障局的官方网站或使用他们的服务平台（如手机APP、微信公众号等）进行查询，看看是否有提供网上办理的选项。如果网上办理服务可用，通常需要按照指导完成身份验证、填写相关表格和上传所需文件等步骤。<br><br>如果网上办理不可用，您需要按照通知中的规定，持本人居民身份证前往社会保障卡服务网点进行正式挂失并办理补卡手续。同时，记得在挂失和补办过程中遵循通知中的指引，确保所有操作符合规定的程序。");
        messages.add("写一手诗？");
        lastRelateQuestionIndex = getLastRelateQuestionIndex(messages.size() - 1, messages.size() -3, messages);
        System.out.println(lastRelateQuestionIndex);
    }
}
