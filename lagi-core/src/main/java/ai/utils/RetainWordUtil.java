package ai.utils;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RetainWordUtil {

    private static final Set<String> set = new HashSet<>();

    static {
        List<String> wordList = JsonFileLoadUtil.readWordListJson("/retain_word.json");
        addWords(wordList);
    }

    public static void addWords(List<String> wordList) {
        set.addAll(wordList);
    }


    public static boolean contains(String message) {
        return set.contains(message);
    }

    public static String replace(String message) {
        if(message == null) {
            return null;
        }
        if(set.isEmpty()) {
            return message;
        }
        StringBuilder stringBuilder = new StringBuilder();
        AtomicInteger count = new AtomicInteger();
        set.forEach(word -> {
            stringBuilder.append("(");
            stringBuilder.append(word);
            stringBuilder.append(")");
            if(count.get() != set.size() -1) {
                stringBuilder.append("|");
            }
            count.getAndIncrement();
        });
        String pattern = stringBuilder.toString();
        return message.replaceAll(pattern, "");
    }



}
