package ai.llm.utils.convert;

import ai.llm.pojo.SenseDataResponse;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.gson.Gson;
import okhttp3.Response;

import java.util.List;
import java.util.stream.Collectors;

public class SenseConvert {

    private static final Gson gson = new Gson();
    public static ChatCompletionResult convert2ChatCompletionResult(String body) {
        if(body == null) {
            return null;
        }
        SenseDataResponse senseDataResponse = gson.fromJson(body, SenseDataResponse.class);
        ChatCompletionResult response = new ChatCompletionResult();
        BeanUtil.copyProperties(senseDataResponse.getData(), response, CopyOptions.create().setIgnoreProperties("choices"));
        List<ChatCompletionChoice> choices = senseDataResponse.getData().getChoices().stream().map(choice -> {
            ChatCompletionChoice chatCompletionChoice = new ChatCompletionChoice();
            chatCompletionChoice.setIndex(choice.getIndex());
            chatCompletionChoice.setFinish_reason(choice.getFinish_reason());
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRole(choice.getRole());
            chatMessage.setContent(choice.getMessage());
            chatCompletionChoice.setMessage(chatMessage);
            return chatCompletionChoice;
        }).collect(Collectors.toList());
        response.setChoices(choices);
        return response;
    }


    public static Integer convertByResponse(Response response) {
        int code = response.code();
        if(code == 200) {
            return 200;
        }
        if(code == 401) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        if(code == 403) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if(code == 429) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if(code == 500) {
            return LLMErrorConstants.SERVER_ERROR;
        }
        if(code == 503) {
            return LLMErrorConstants.OTHER_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

}
