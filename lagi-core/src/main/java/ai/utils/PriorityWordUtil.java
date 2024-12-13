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
        addWords(wordList);
    }

    public static void addWords(List<String> wordList) {
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
            double cons = 1.000007639697643;
            Double cp1 = c1.getDistance() / (b1 ? cons: 1);
            Double cp2 = c2.getDistance() / (b2 ? cons: 1);
            return cp1.compareTo(cp2);
        }).collect(Collectors.toList());
    }

}
