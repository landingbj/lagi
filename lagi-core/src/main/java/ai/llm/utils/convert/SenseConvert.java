package ai.llm.utils.convert;

import ai.common.exception.RRException;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;
import okhttp3.Response;

public class SenseConvert {
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

    public static int convertByInt(int code) {
        if (code == 200) {
            return 200;
        }
        if (code == 401) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        if (code == 403) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if (code == 429) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if (code == 500) {
            return LLMErrorConstants.SERVER_ERROR;
        }
        if (code == 503) {
            return LLMErrorConstants.OTHER_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static int convertByResponse(Response response) {
        return convertByInt(response.code());
    }

    public static RRException convert2RResponse(Response response) {
        return new RRException(convertByResponse(response), response.message());
    }

    public static ChatCompletionResult convert2ChatCompletionResult(String body) {
        return gson.fromJson(body, ChatCompletionResult.class);
    }

    public static ChatCompletionResult convertStreamLine2ChatCompletionResult(String data) {
        if (data == null || data.equals("[DONE]")) {
            return null;
        }
        ChatCompletionResult result = gson.fromJson(data, ChatCompletionResult.class);
        result.getChoices().forEach(choice -> {
            choice.setMessage(choice.getDelta());
            choice.setDelta(null);
        });
        return result;
    }
}
