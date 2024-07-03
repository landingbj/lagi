package ai.intent.impl;


import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.enums.IntentTypeEnum;
import ai.intent.pojo.IntentResult;
import ai.medusa.impl.CompletionCache;
import ai.medusa.utils.PromptCacheTrigger;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.StoppingWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class SampleIntentServiceImpl implements IntentService {

    private static final String punctuations = "[\\.,;!\\?，。；！？]";



    private List<String> splitByPunctuation(String content) {
        String[] split = content.split(punctuations);
        return Arrays.stream(split).filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }

    private IntentTypeEnum detectType(ChatCompletionRequest chatCompletionRequest) {
        String lastMessage = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        List<String> segments = splitByPunctuation(lastMessage);
        IntentTypeEnum[] enums = IntentTypeEnum.values();
        for(IntentTypeEnum e : enums) {
            if(e.matches(lastMessage ,segments)) {
                return e;
            }
        }
        return IntentTypeEnum.TEXT;
    }

    @Override
    public IntentResult detectIntent(ChatCompletionRequest chatCompletionRequest) {
        IntentTypeEnum intentTypeEnum = detectType(chatCompletionRequest);
        IntentResult intentResult = new IntentResult();
        intentResult.setType(intentTypeEnum.getName());
        if(intentTypeEnum != IntentTypeEnum.TEXT
                || chatCompletionRequest.getMax_tokens() <= 0) {
            return intentResult;
        }
        int lastIndex = 0;
//        intentResult.setStatus(IntentStatusEnum.getStatusByContents(chatCompletionRequest.getMessages().stream().map(ChatMessage::getContent).collect(Collectors.toList()), punctuations).getName());

        CompletionCache completionCache = CompletionCache.getInstance();
        PromptCacheTrigger promptCacheTrigger = new PromptCacheTrigger(completionCache);
        List<String> questionList = chatCompletionRequest.getMessages().stream()
                .filter(m->"user".equals(m.getRole()))
                .map(ChatMessage::getContent).collect(Collectors.toList());
        lastIndex = promptCacheTrigger.analyzeChatBoundariesForIntent(chatCompletionRequest);
        if(lastIndex == (questionList.size()-1)) {
            intentResult.setStatus(IntentStatusEnum.COMPLETION.getName());
        } else {
            intentResult.setStatus(IntentStatusEnum.CONTINUE.getName());
            intentResult.setContinuedIndex(lastIndex * 2);
        }
        List<Integer> stoppingIndex = StoppingWordUtil.getStoppingIndex(chatCompletionRequest.getMessages());
        if(!stoppingIndex.isEmpty() && intentResult.getContinuedIndex() != null) {
            lastIndex = stoppingIndex.get(stoppingIndex.size() - 1);
            if (lastIndex != chatCompletionRequest.getMessages().size() - 1) {
                intentResult.setStatus(IntentStatusEnum.CONTINUE.getName());
                intentResult.setContinuedIndex(lastIndex);
            } else {
                intentResult.setStatus(IntentStatusEnum.COMPLETION.getName());
            }
        }
        return intentResult;
    }



    public static void main(String[] args) {
//        SampleIntentServiceImpl sampleIntentService = new SampleIntentServiceImpl();
//        List<String> strings = sampleIntentService.splitByPunctuation("你好。画一张狗狗图");
//        System.out.println(strings);
//        IntentResult intentResult = sampleIntentService.detectIntent(strings);
//        System.out.println(intentResult);
    }
}
