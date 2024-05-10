package ai.intent.enums;

import ai.core.matrix.LCS;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public enum IntentStatusEnum {

    CONTINUE("continue"),
    COMPLETION("completion");

    private String name;

    IntentStatusEnum(String name) {
        this.name = name;
    }


    public static IntentStatusEnum getStatusByContents(List<String> contents, String punctuations, double percent) {
        if(contents == null || contents.isEmpty()) {
            return IntentStatusEnum.COMPLETION;
        };
        String lastContent = contents.remove(contents.size() - 1);
        if(percent > 1.0 || percent < 0.0) {
            percent = 0.5D;
        }
        int threshold = (int) (lastContent.length() * percent);
        List<String[]> collect = contents.stream().map(i -> LCS.lcs(i, lastContent, punctuations, threshold, false)).collect(Collectors.toList());
        if(!collect.isEmpty()) {
            return IntentStatusEnum.CONTINUE;
        }
        return IntentStatusEnum.COMPLETION;
    }


    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        strings.add("非北京户籍如何办理犯罪记录查询");
        strings.add("如何办理");
        IntentStatusEnum statusByContents = IntentStatusEnum.getStatusByContents(strings, "[\\.,;!\\?，。；！？]", 5);
        System.out.println(statusByContents.getName());
    }

}
