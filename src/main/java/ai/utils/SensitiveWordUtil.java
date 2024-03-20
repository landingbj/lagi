package ai.utils;

import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SensitiveWordUtil {
    private static final AhoCorasick ahoCorasick = new AhoCorasick();

    static {
        List<String> sensitiveWordList = readSensitiveWordJson();
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

    private static List<String> readSensitiveWordJson() {
        String respath = "/sensitive_word.json";
        String content = "{}";

        try (InputStream in = SensitiveWordUtil.class.getResourceAsStream(respath);) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<String> tempResult = new Gson().fromJson(content, listType);
        List<String> result = new ArrayList<>();
        for (String word : tempResult) {
            result.add(word.toLowerCase());
        }
        return result;
    }
}
