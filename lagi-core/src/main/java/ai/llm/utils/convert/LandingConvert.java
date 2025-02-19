package ai.llm.utils.convert;

import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;
import okhttp3.Response;

public class LandingConvert {
    private static final Gson gson = new Gson();

    public static int convert(Object object) {
        if (object instanceof Integer) {
            return convertByInt((int) object);
        }
        if (object instanceof Response) {
            return convertByResponse((Response) object);
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static int convertByInt(int errorCode) {
        return errorCode;
    }

    public static int convertByResponse(Response response) {
        return convert(response.code());
    }

    public static ChatCompletionResult convert2ChatCompletionResult(String body) {
        return gson.fromJson(body, ChatCompletionResult.class);
    }

    public static ChatCompletionResult convertSteamLine2ChatCompletionResult(String body) {
        if (body.equals("[DONE]")) {
            return null;
        }
        ChatCompletionResult result = gson.fromJson(body, ChatCompletionResult.class);
        result.getChoices().forEach(choice -> {
            choice.setMessage(choice.getMessage());
        });
        return result;
    }
}
