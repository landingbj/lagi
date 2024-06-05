package ai.intent.impl;


import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.enums.IntentTypeEnum;
import ai.intent.pojo.IntentResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.StoppingWordUtil;
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


    @Override
    public IntentResult detectIntent(List<ChatMessage> chatMessages) {
        List<String> messages = chatMessages.stream().map(ChatMessage::getContent).collect(Collectors.toList());
        if(messages.isEmpty()) {
            return null;
        }
        String newMessage = messages.get(messages.size() - 1);
        List<String> segments = splitByPunctuation(newMessage);
        IntentTypeEnum[] enums = IntentTypeEnum.values();
        IntentResult intentResult = new IntentResult();
        for(IntentTypeEnum e : enums) {
            if(e.matches(newMessage ,segments)) {
                intentResult.setType(e.getName());
            }
        }
        if(intentResult.getType() == null) {
            intentResult.setType(IntentTypeEnum.TEXT.getName());
        }
        intentResult.setStatus(IntentStatusEnum.getStatusByContents(messages, punctuations).getName());
        // TODO 2024/6/4 当状态是 completion 是获取停止词没有不处理， 有一个:设置为continued, 设置continued index, 有多个 返回最新的不同的一个 的 continued index
        if(intentResult.getStatus().equals(IntentStatusEnum.COMPLETION.getName())) {
            List<Integer> stoppingIndex = StoppingWordUtil.getStoppingIndex(chatMessages);
            if(stoppingIndex.isEmpty()) {
                return intentResult;
            }
            int lastIndex = stoppingIndex.get(stoppingIndex.size() - 1);
            intentResult.setStatus(IntentStatusEnum.CONTINUE.getName());
            intentResult.setContinuedIndex(lastIndex);
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
