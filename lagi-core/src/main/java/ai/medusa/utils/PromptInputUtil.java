package ai.medusa.utils;

import ai.medusa.pojo.PromptInput;

import java.util.List;

public class PromptInputUtil {

    public static String getFirstPrompt(PromptInput promptInput) {
        List<String> promptList = promptInput.getPromptList();
        if(promptList == null || promptList.isEmpty()) {
            return null;
        }
        return promptList.get(0);
    }

    public static String getNewestPrompt(PromptInput promptInput) {
        List<String> promptList = promptInput.getPromptList();
        return promptList.get(promptList.size() - 1);
    }

    public static String getLastPrompt(PromptInput promptInput) {
        if (promptInput.getPromptList().size() < 2) {
            return null;
        }
        return promptInput.getPromptList().get(promptInput.getPromptList().size() - 2);
    }

    public static PromptInput getLastPromptInput(PromptInput promptInput) {
        List<String> promptList = promptInput.getPromptList();
        if (promptList.size() < 2) {
            return null;
        }
        return PromptInput.builder()
                .parameter(promptInput.getParameter())
                .promptList(promptList.subList(0, promptList.size() - 1))
                .build();
    }
}
