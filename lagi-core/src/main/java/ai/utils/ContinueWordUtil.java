package ai.utils;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContinueWordUtil {

    private static final List<String> patterns = new ArrayList<>();

    static {
        List<String> wordList = JsonFileLoadUtil.readWordListJson("/continue_word.json");
        addWords(wordList);
    }

    public static void addWords(List<String> wordList) {
        patterns.addAll(wordList);
    }

    public static boolean containsStoppingWorlds(String msg) {
        msg = msg.trim();
        for(String pattern : patterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher matcher = p.matcher(msg);
            if(matcher.find()) {
                return true;
            }
        }
        return false;
    }


}
