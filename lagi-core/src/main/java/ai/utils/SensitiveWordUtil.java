package ai.utils;

import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;

import java.util.List;

public class SensitiveWordUtil {
    private static final AhoCorasick ahoCorasick = new AhoCorasick();

    static {
        List<String> sensitiveWordList = JsonFileLoadUtil.readWordListJson("/sensitive_word.json");
        ahoCorasick.addWords(sensitiveWordList);
    }

    public static boolean containSensitiveWord(ChatCompletionResult chatCompletionResult) {
        List<ChatCompletionChoice> choices = chatCompletionResult.getChoices();
        for (ChatCompletionChoice choice : choices) {
            String message = choice.getMessage().getContent().toLowerCase();
            if (ahoCorasick.containsAny(message)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containSensitiveWord(String message) {
        if (ahoCorasick.containsAny( message.toLowerCase())) {
            return true;
        }
        return false;
    }

}
