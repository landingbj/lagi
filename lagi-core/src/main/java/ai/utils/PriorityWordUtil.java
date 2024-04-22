package ai.utils;


import ai.common.pojo.IndexSearchData;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;

import java.util.List;
import java.util.stream.Collectors;

public class PriorityWordUtil  {

    private static final AhoCorasick ahoCorasick = new AhoCorasick();

    static {
        List<String> wordList = JsonFileLoadUtil.readWordListJson("/priority_word.json");
        ahoCorasick.addWords(wordList);
    }

    public static List<ChatCompletionChoice> sortByPriorityWord(ChatCompletionResult chatCompletionResult) {
        List<ChatCompletionChoice> choices = chatCompletionResult.getChoices();
        return choices.stream().sorted((c1, c2)->{
            String m1 = c1.getMessage().getContent().toLowerCase();
            String m2 = c2.getMessage().getContent().toLowerCase();
            if(ahoCorasick.containsAny(m1)) {
                return 1;
            }
            if(ahoCorasick.containsAny(m2)) {
                return -1;
            }
            return 0;
        }).collect(Collectors.toList());
    }

    public static boolean containPriorityWord(String message) {
        if (ahoCorasick.containsAny(message.toLowerCase())) {
            return true;
        }
        return false;
    }


    public static List<IndexSearchData> sortByPriorityWord(List<IndexSearchData> search) {
        return search.stream().sorted((c1, c2)->{
            String m1 = c1.getText().toLowerCase();
            String m2 = c2.getText().toLowerCase();
            boolean b1 = ahoCorasick.containsAny(m1);
            boolean b2 = ahoCorasick.containsAny(m2);
            if(b1 && b2) {
                return 0;
            }
            if(b1) {
                return -1;
            }
            if(b2) {
                return 1;
            }
            return 0;
        }).collect(Collectors.toList());
    }


}
