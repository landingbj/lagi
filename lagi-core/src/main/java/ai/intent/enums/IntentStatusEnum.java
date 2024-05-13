package ai.intent.enums;

import ai.core.matrix.LCS;
import lombok.Getter;
import org.ansj.splitWord.analysis.BaseAnalysis;

import java.util.*;

@Getter
public enum IntentStatusEnum {

    CONTINUE("continue"),
    COMPLETION("completion");

    private String name;

    IntentStatusEnum(String name) {
        this.name = name;
    }


    public static IntentStatusEnum getStatusByContents(List<String> contents, String punctuations) {
        if(contents == null  || contents.size() < 2) {
            return IntentStatusEnum.COMPLETION;
        };
        String lastContent = contents.get(contents.size() - 1);
        long count  = contents.stream()
                .limit(contents.size() - 1)
                .map(i -> {
                    String context = BaseAnalysis.parse(i).toStringWithOutNature();
                    String question = BaseAnalysis.parse(lastContent).toStringWithOutNature();
                    return LCS.lcs(context, question, punctuations, 0, false);
                })
                .filter(arr -> arr.length > 0).count();
        if(count > 0) {
            return IntentStatusEnum.CONTINUE;
        }
        return IntentStatusEnum.COMPLETION;
    }


    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        strings.add("香港人如何在朝阳区办理犯罪记录查询 ");
        strings.add("1. 准备《犯罪记录查询申请表（中国公民）》，可以在以下网址下载空表：https://banshi.beijing.gov.cn/pubtask/task/1/110105000000/dad315c9-8697-4ea1-8eac-1bca0b28ffba.html?locationCode=110105000000#Guide-btn。\n" +
                "\n" +
                "2. 准备港澳台居民居住证。\n" +
                "\n" +
                "3. 准备港澳居民往来内地通行证。\n" +
                "\n" +
                "4. 如果委托他人代为查询，需要另行准备委托书和受托人的身份证件。\n" +
                "\n" +
                "5. 如果在一个自然年度内申请查询超过3次（不含3次），需提交查询犯罪记录合理用途材料。\n" +
                "\n" +
                "6. 如果对查询结果有异议，需要填写并提交犯罪记录复查申请表（中国公民），下载地址与申请表相同。\n" +
                "\n" +
                "请注意，具体要求和流程可能会有所变化，建议在办理前咨询当地相关部门。");
        strings.add("如何办理");
        IntentStatusEnum statusByContents = IntentStatusEnum.getStatusByContents(strings, "[ \\.,;!\\?，。；！？]");
        System.out.println(statusByContents.getName());
    }

}
