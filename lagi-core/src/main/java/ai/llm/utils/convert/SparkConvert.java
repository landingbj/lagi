package ai.llm.utils.convert;

import ai.common.exception.RRException;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;
import okhttp3.Response;

public class SparkConvert {
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

    public static Integer convertByInt(Integer errorCode) {
        if (errorCode == 10001 || errorCode == 10002 || errorCode == 10003 || errorCode == 10004
                || errorCode == 10005
                || errorCode == 10163
                || errorCode == 10907
        ) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if (errorCode == 10006 || errorCode == 10008 || errorCode == 11200) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if (errorCode == 10007 || errorCode == 11202) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if (errorCode == 11201 || errorCode == 11203) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
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
