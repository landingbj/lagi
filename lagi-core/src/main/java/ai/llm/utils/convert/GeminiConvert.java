package ai.llm.utils.convert;

import ai.common.exception.RRException;
import ai.llm.pojo.GeminiResponse;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.openai.pojo.Usage;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class GeminiConvert {

    private static final Gson gson = new Gson();

    public static int convert(Object object) {
        if(object instanceof  Integer) {
            return convertByInt((int) object);
        }
        if(object instanceof Response) {
            return convertByResponse((Response) object);
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    /**
     * reference https://ai.google.dev/gemini-api/docs/troubleshooting?hl=zh-cn
     * @param errorCode http status code
     * @return lagi error code
     */
    public static int convertByInt(int errorCode) {
        if(errorCode == 400) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if(errorCode == 403) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        if(errorCode == 404) {
            return LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR;
        }
        if(errorCode == 429) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if(errorCode == 500) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if(errorCode == 503) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if(errorCode == 504) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static int convertByResponse(Response response) {
        return convert(response.code());
    }

    public static RRException convert2RResponse(Response response) {
        return new RRException(convertByResponse(response), response.message());
    }


    public static ChatCompletionResult convertStringResponse(String body) {
        GeminiResponse response = gson.fromJson(body, GeminiResponse.class);
        return convertResponse(response);
    }

    public static ChatCompletionResult convertResponse(GeminiResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        for (GeminiResponse.Candidate candidate : response.getCandidates()) {
            ChatCompletionChoice choice = new ChatCompletionChoice();
            choice.setIndex(candidate.getIndex());
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(candidate.getContent().getParts().get(0).getText());
            chatMessage.setRole(LagiGlobal.LLM_ROLE_ASSISTANT);
            choice.setMessage(chatMessage);
            choice.setFinish_reason("stop");
            choices.add(choice);
        }
        result.setChoices(choices);
        Usage usage = new Usage();
        usage.setCompletion_tokens(response.getUsageMetadata().getCandidatesTokenCount());
        usage.setPrompt_tokens(response.getUsageMetadata().getPromptTokenCount());
        usage.setTotal_tokens(response.getUsageMetadata().getTotalTokenCount());
        result.setUsage(usage);
        return result;
    }

}
