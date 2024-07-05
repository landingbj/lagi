package ai.llm.utils;

import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class DummyCompletionUtil {
    private static final Gson gson = new Gson();

    public static ChatCompletionResult getDummyCompletion() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String currentDatetime = dateFormat.format(new Date());
        System.out.println("Current datetime: " + currentDatetime);
        String message = currentDatetime + " " + UUID.randomUUID();
        String json = "{\"created\":1719495617,\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\"," +
                "\"content\":\"" + message + "\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":38," +
                "\"completion_tokens\":8,\"total_tokens\":46}}\n";
        return gson.fromJson(json, ChatCompletionResult.class);
    }

    public static void main(String[] args) {
        ChatCompletionResult result = getDummyCompletion();
        System.out.println(result.getChoices().get(0).getMessage().getContent());
    }
}
