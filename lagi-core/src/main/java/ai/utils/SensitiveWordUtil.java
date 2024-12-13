package ai.utils;

import ai.common.pojo.WordRule;
import ai.common.pojo.WordRules;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveWordUtil {

    private static Map<String, WordRule> ruleMap = new HashMap<>();

    static {

        WordRules wordRules = JsonFileLoadUtil.readWordLRulesList("/sensitive_word.json", WordRules.class);
        pushWordRule(wordRules);
    }

    public static void pushWordRule(WordRules wordRules) {
        if(wordRules != null) {
            if(wordRules.getRules() != null) {
                wordRules.getRules().stream()
                        .filter(r-> StrUtil.isNotBlank(r.getRule()))
                        .peek(r-> {
                            r.setRule(r.getRule().toLowerCase());
                            if(r.getMask() == null) {
                                r.setMask(wordRules.getMask());
                            }
                            if(r.getLevel() == null) {
                                r.setLevel(wordRules.getLevel());
                            }
                        })
                        .forEach(r->{
                            ruleMap.put(r.getRule(), r);
                        });
            }
        }
    }


    public static ChatCompletionResult filter(ChatCompletionResult chatCompletionResult) {
        Set<String> rules = ruleMap.keySet();
        if(chatCompletionResult == null || chatCompletionResult.getChoices() ==null || chatCompletionResult.getChoices().isEmpty()) {
            return chatCompletionResult;
        }
        for(ChatCompletionChoice choice: chatCompletionResult.getChoices()) {
            for (String rule : rules) {
                Pattern p = Pattern.compile(rule);
                if(choice.getMessage() ==null || choice.getMessage().getContent() == null) {
                    continue;
                }
                String message = choice.getMessage().getContent().toLowerCase();
                Matcher matcher = p.matcher(choice.getMessage().getContent().toLowerCase());
                if(matcher.find()) {
                    WordRule wordRule = ruleMap.get(rule);
                    if(wordRule != null) {
                        if(wordRule.getLevel() == 1) {
                            choice.getMessage().setContent("");
                        }else if(wordRule.getLevel() == 2) {
                            String s = message.replaceAll(rule, wordRule.getMask());
                            choice.getMessage().setContent(s);
                        } else if(wordRule.getLevel() == 3) {
                            String s = message.replaceAll(rule, "");
                            choice.getMessage().setContent(s);
                        }
                    }
                }
            }
        }
        return chatCompletionResult;
    }

    public static String filter(String message) {
        Set<String> rules = ruleMap.keySet();
        String lowerCase = message.toLowerCase();
        for (String rule : rules) {
            Pattern p = Pattern.compile(rule);
            Matcher matcher = p.matcher(lowerCase);
            if(matcher.find()) {
                WordRule wordRule = ruleMap.get(rule);
                if(wordRule != null) {
                    if(wordRule.getLevel() == 1) {
                        return "";
                    }else if(wordRule.getLevel() == 2) {
                       return lowerCase.replaceAll(rule, wordRule.getMask());
                    } else if(wordRule.getLevel() == 3) {
                        return lowerCase.replaceAll(rule, "");
                    }
                }
            }
        }
        return message;
    }


    public static void main(String[] args) {

    }

}
