package ai.intent.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public enum IntentTypeEnum {

    IMAGE("image", new String[]{"生成.*?图$",
            "生成.*?图片$",
            "生成.*?图像$",
            "画1张.*?图$",
            "画1张.*?图片$",
            "画1张.*?图像$",
            "画一张.*?图$",
            "画一张.*?图片$",
            "画一张.*?图像$",
            "画.*?张.*?图$",
            "画.*?张.*?图片$",
            "画.*?张.*?图像$"}),
    VIDEO("svd_by_text", new String[]{"生成.*?视频$",
            "生成视频.*?",}),
    TRANSLATE("translate", new String[]{"翻译.*?语",
            "翻译.*?文",
            "语.*?翻译",
            "文.*?翻译",
            "英语.*?",
            "英文.*?"}),
    TIME("time", new String[]{"前天.*?星期几",
            "昨天.*?星期几",
            "今天.*?星期几",
            "明天.*?星期几",
            "后天.*?星期几",
            "前天.*?几号",
            "昨天.*?几号",
            "今天.*?几号",
            "明天.*?几号",
            "后天.*?几号",
            "现在.*?几点",
            "现在.*?几点了",
            "几点了",}),
    TEXT("text", new String[]{}),;


    private final String name;

    private final String[] patterns;

    IntentTypeEnum(String name, String[] patterns) {
        this.name = name;
        this.patterns = patterns;
    }

    public boolean matches(List<String> segments) {
        for (String segment : segments) {
            for (String pattern : patterns) {
                if (Pattern.matches(pattern, segment)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        List<String> ls = new ArrayList<>();
        ls.add("画一张狗狗图");
        boolean matches = IMAGE.matches(ls);
        System.out.println(matches);
    }

}
