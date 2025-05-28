package ai.vl.adapter.impl;

import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.GptConvert;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.VlChatCompletionRequest;
import ai.vl.adapter.VlAdapter;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenAiAdapter extends ModelService implements VlAdapter  {

    private static final int HTTP_TIMEOUT = 15 * 1000;
    @Override
    public ChatCompletionResult completions(VlChatCompletionRequest request) {
        setDefaultModel(request);
        LlmApiResponse completions = OpenAiApiUtil.completions(getApiKey(), getEndpoint(), HTTP_TIMEOUT, request, GptConvert::convert2ChatCompletionResult, GptConvert::convertByResponse);
        if (completions.getCode() != 200) {
            log.error("openai api : code{}  error  {}", completions.getCode(), completions.getMsg());
            throw new RuntimeException(completions.getMsg());
        }
        return completions.getData();
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(VlChatCompletionRequest request) {
        setDefaultModel(request);
        LlmApiResponse completions = OpenAiApiUtil.streamCompletions(getApiKey(), getEndpoint(), HTTP_TIMEOUT, request, GptConvert::convert2ChatCompletionResult, GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            log.error("openai  stream api : code{}  error  {}", completions.getCode(), completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getStreamData();
    }

    private void setDefaultModel(VlChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
}
