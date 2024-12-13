package ai.utils;


import ai.openai.pojo.ChatMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StoppingWordUtil {

    private static final AhoCorasick ahoCorasick = new AhoCorasick();
    private static final List<String> patterns = new ArrayList<>();

    static {
        List<String> wordList = JsonFileLoadUtil.readWordListJson("/stopping_word.json");
        addWords(wordList);
    }

    public static void addWords(List<String> wordList) {
        if(wordList == null) {
            return;
        }
        ahoCorasick.addWords(wordList);
        patterns.addAll(wordList);
    }

    public static boolean containsStoppingWorlds(String msg) {
        for(String pattern : patterns) {
            Pattern p = Pattern.compile(pattern);
            Matcher matcher = p.matcher(msg);
            if(matcher.find()) {
                return true;
            }
        }
        return false;
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

                    for(String pattern : patterns) {
                        Pattern p = Pattern.compile(pattern);
                        Matcher matcher = p.matcher(chatMessage.getContent());
                        if(matcher.find()) {
                            return true;
                        }
                    }
                    return false;
                } )
                .boxed()
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String text = "我的社保卡丢失了，该怎么办？";
        String regex = "社保";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            System.out.println("匹配成功！");
        } else {
            System.out.println("匹配失败！");
        }
    }

}
