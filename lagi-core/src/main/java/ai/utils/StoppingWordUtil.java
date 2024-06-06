package ai.utils;


import ai.openai.pojo.ChatMessage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StoppingWordUtil {

    private static final AhoCorasick ahoCorasick = new AhoCorasick();

    static {
        List<String> wordList = JsonFileLoadUtil.readWordListJson("/stopping_word.json");
        ahoCorasick.addWords(wordList);
    }


    public static List<Integer> getStoppingIndex(List<ChatMessage> messages) {
        if(messages == null) {
            return Collections.emptyList();
        }
        return IntStream.range(0, messages.size())
                .filter(i-> {
                    ChatMessage chatMessage = messages.get(i);
                    if(!chatMessage.getRole().equals("user")) {
                        return false;
                    }
                    return ahoCorasick.containsAny(messages.get(i).getContent());
                } )
                .boxed()
                .collect(Collectors.toList());
    }


}
