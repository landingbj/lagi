package ai.llm.utils.convert;

import ai.common.exception.RRException;
import ai.llm.utils.LLMErrorConstants;
import okhttp3.Response;

public class OpenAiConvert {

    public static int convert(Object object) {
        if(object instanceof  Integer) {
            return convertByInt((int) object);
        }
        if(object instanceof Response) {
            return convertByResponse((Response) object);
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static int convertByInt(int errorCode) {
        if(errorCode == 400) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if(errorCode == 401) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        if(errorCode == 403) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if(errorCode == 404) {
            return LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR;
        }
        if(errorCode == 429) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if(errorCode == 500) {
            return LLMErrorConstants.SERVER_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static int convertByResponse(Response response) {
        return convert(response.code());
    }

    public static RRException convert2RResponse(Response response) {
        return new RRException(convertByResponse(response), response.message());
    }
}
