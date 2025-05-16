package ai.medusa.utils;

import ai.medusa.pojo.PromptInput;

import java.util.ArrayList;
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

    public static void setApproximateTemperature(PromptInput promptInput) {
        double approximateTemperature = getApproximateTemperature(promptInput);
        promptInput.getParameter().setTemperature(approximateTemperature);
    }

    private static double getApproximateTemperature(PromptInput promptInput) {
        double target = promptInput.getParameter().getTemperature();
        if (PromptCacheConfig.TEMPERATURE_TOLERANCE == null) {
            return target;
        }
        List<Double> toleranceList = new ArrayList<>(PromptCacheConfig.TEMPERATURE_TOLERANCE);
        int left = 0;
        int right = toleranceList.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            double midVal = toleranceList.get(mid);
            if (midVal < target) {
                left = mid + 1;
            } else if (midVal > target) {
                right = mid - 1;
            } else {
                return midVal;
            }
        }
        if (left >= toleranceList.size()) return toleranceList.get(toleranceList.size() - 1);
        if (right < 0) return toleranceList.get(0);
        double leftVal = toleranceList.get(left);
        double rightVal = toleranceList.get(right);
        return Math.abs(leftVal - target) < Math.abs(rightVal - target) ? leftVal : rightVal;
    }
}
