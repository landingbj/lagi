package ai.llm.adapter.impl;


import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.GptConvert;

import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LLM(modelNames = {"gpt-3.5-turbo","gpt-4-1106-preview"})
public class GPTAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GPTAdapter.class);
    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final int HTTP_TIMEOUT = 15 * 1000;

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        LlmApiResponse completions = OpenAiApiUtil.completions(apiKey, COMPLETIONS_URL, HTTP_TIMEOUT, chatCompletionRequest,
                GptConvert::convert2ChatCompletionResult,
                GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error(completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        LlmApiResponse completions = OpenAiApiUtil.streamCompletions(apiKey, COMPLETIONS_URL, HTTP_TIMEOUT, chatCompletionRequest,
                GptConvert::convertSteamLine2ChatCompletionResult,
                GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("open ai stream api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getStreamData();
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
}
