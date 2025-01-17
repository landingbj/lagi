package ai.llm.utils.convert;

import ai.llm.pojo.ClaudeStreamResponse;
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

public class ClaudeConvert {
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

    /**
     * reference https://docs.anthropic.com/en/api/errors
     *
     * @param errorCode http status code
     * @return lagi error code
     */
    public static int convertByInt(int errorCode) {
        if (errorCode == 400) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if (errorCode == 401) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        if (errorCode == 403) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if (errorCode == 404) {
            return LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR;
        }
        if (errorCode == 429) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if (errorCode == 500) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

    public static int convertByResponse(Response response) {
        return convert(response.code());
    }

    public static ChatCompletionResult convertStreamLine2ChatCompletionResult(String body) {
        ClaudeStreamResponse response = gson.fromJson(body, ClaudeStreamResponse.class);
        return convertResponse(response);
    }

    private static ChatCompletionResult convertResponse(ClaudeStreamResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        if ("message_start".equals(response.getType())) {
            result.setId(response.getMessage().getId());
        }
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        if ("content_block_delta".equals(response.getType())) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(response.getDelta().getText());
            chatMessage.setRole(LagiGlobal.LLM_ROLE_ASSISTANT);
            choice.setMessage(chatMessage);
        } else {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent("");
            chatMessage.setRole(LagiGlobal.LLM_ROLE_ASSISTANT);
            choice.setMessage(chatMessage);
            if ("message_delta".equals(response.getType())) {
                choice.setFinish_reason(response.getDelta().getStop_reason());
                Usage usage = new Usage();
                usage.setCompletion_tokens(response.getUsage().getOutput_tokens());
                result.setUsage(usage);
            }
        }
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        return result;
    }
}
