package ai.llm.utils.convert;

import ai.common.exception.RRException;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.common.Status;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.gson.Gson;
import okhttp3.Response;


public class QwenConvert {
    private static final Gson gson = new Gson();

    public static Integer convertByResponse(Response response) {
        int code = response.code();
        if (code == 400) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if (code == 401) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        if (code == 403) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if (code == 404) {
            return LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR;
        }
        if (code == 408) {
            return LLMErrorConstants.TIME_OUT;
        }
        if (code == 429) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if (code == 500) {
            return LLMErrorConstants.SERVER_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static RRException convert2RRexception(Exception e) {
        if (e instanceof NoApiKeyException) {
            return new RRException(LLMErrorConstants.INVALID_AUTHENTICATION_ERROR, e.getMessage());
        }
        if (e instanceof InputRequiredException) {
            return new RRException(LLMErrorConstants.INVALID_REQUEST_ERROR, e.getMessage());
        }
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            Status status = apiException.getStatus();
            Integer code = status.getStatusCode();
            String msg = JSONUtil.toJsonStr(status);
//            https://help.aliyun.com/zh/model-studio/developer-reference/error-code?spm=a2c4g.11186623.0.i1
            if (code == 400) {
                return new RRException(LLMErrorConstants.INVALID_REQUEST_ERROR, msg);
            }
            if (code == 401) {
                return new RRException(LLMErrorConstants.INVALID_AUTHENTICATION_ERROR, msg);
            }
            if (code == 403) {
                return new RRException(LLMErrorConstants.PERMISSION_DENIED_ERROR, msg);
            }
            if (code == 404) {
                return new RRException(LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR, msg);
            }
            if (code == 408) {
                return new RRException(LLMErrorConstants.TIME_OUT, msg);
            }
            if (code == 429) {
                return new RRException(LLMErrorConstants.RATE_LIMIT_REACHED_ERROR, msg);
            }
            if (code == 500) {
                return new RRException(LLMErrorConstants.SERVER_ERROR, msg);
            }
        }
        return new RRException(LLMErrorConstants.OTHER_ERROR, "{\"error\": \"unknown error\"}");
    }

    public static ChatCompletionResult convert2ChatCompletionResult(String body) {
        if (body == null) {
            return null;
        }
        return gson.fromJson(body, ChatCompletionResult.class);
    }
}
