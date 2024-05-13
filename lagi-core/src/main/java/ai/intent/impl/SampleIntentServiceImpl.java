package ai.intent.impl;


import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.enums.IntentTypeEnum;
import ai.intent.pojo.IntentResult;
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
    public IntentResult detectIntent(List<String> messages) {
        if(messages == null || messages.isEmpty()) {
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
        return intentResult;
    }

    public static void main(String[] args) {
        SampleIntentServiceImpl sampleIntentService = new SampleIntentServiceImpl();
        List<String> strings = sampleIntentService.splitByPunctuation("你好。画一张狗狗图");
        System.out.println(strings);
        IntentResult intentResult = sampleIntentService.detectIntent(strings);
        System.out.println(intentResult);
    }
}
