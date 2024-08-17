package ai.utils;

import java.util.regex.Pattern;

public class StrFilterUtil {


    public static String filterPunctuations(String input) {
        String regexEnglishPunctuations = "[\\p{Punct}]";
        String regexChinesePunctuations = "[\\u3000-\\u303F\\uFF00-\\uFFEF]";

        String regex = "(" + regexEnglishPunctuations + ")|(" + regexChinesePunctuations + ")";

        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(input).replaceAll("");
    }

}
