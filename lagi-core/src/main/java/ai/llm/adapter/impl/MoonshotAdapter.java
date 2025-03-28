package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.MoonshotConvert;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;

import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@LLM(modelNames = {"moonshot-v1-8k","moonshot-v1-32k","moonshot-v1-128k"})
public class MoonshotAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MoonshotAdapter.class);
    private static final int HTTP_TIMEOUT = 15 * 1000;
    private static final String COMPLETIONS_URL = "https://api.moonshot.cn/v1/chat/completions";


    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultField(chatCompletionRequest);
        LlmApiResponse completions = OpenAiApiUtil.completions(apiKey, COMPLETIONS_URL, HTTP_TIMEOUT, chatCompletionRequest,
                MoonshotConvert::convertChatCompletionResult, MoonshotConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("moonshot  api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
    }



    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultField(chatCompletionRequest);
        LlmApiResponse completions = OpenAiApiUtil.streamCompletions(apiKey, COMPLETIONS_URL, HTTP_TIMEOUT, chatCompletionRequest,
                MoonshotConvert::convertStreamLine2ChatCompletionResult, MoonshotConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("moonshot api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getStreamData();
    }
}
