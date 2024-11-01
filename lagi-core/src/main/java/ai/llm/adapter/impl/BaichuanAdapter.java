package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.BaiChuanConvert;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@LLM(modelNames = {"Baichuan2-Turbo","Baichuan2-Turbo-192k","Baichuan2-53B"})
public class BaichuanAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BaichuanAdapter.class);
    private static final int HTTP_TIMEOUT = 5 * 1000;
    private static final String COMPLETIONS_URL = "https://api.baichuan-ai.com/v1/chat/completions";


    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        LlmApiResponse llmApiResponse = llmCompletions(chatCompletionRequest);
        if(llmApiResponse.getCode() != 200) {
            logger.error("baichuan2 api code:{}  error:{} ", llmApiResponse.getCode(),  llmApiResponse.getMsg());
            throw new RRException(llmApiResponse.getCode(), llmApiResponse.getMsg());
        }
        return llmApiResponse.getData();
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        LlmApiResponse llmApiResponse = llmCompletions(chatCompletionRequest);
        if(llmApiResponse.getCode() != 200) {
            logger.error("baichuan2 stream  api code:{}  error:{} ", llmApiResponse.getCode(),  llmApiResponse.getMsg());
            throw new RRException(llmApiResponse.getCode(), llmApiResponse.getMsg());
        }
        return llmApiResponse.getStreamData();
    }

    public LlmApiResponse llmCompletions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        LlmApiResponse completions;
        if(chatCompletionRequest.getStream()) {
            completions = OpenAiApiUtil.streamCompletions(getApiKey(),
                    COMPLETIONS_URL,
                    HTTP_TIMEOUT,
                    chatCompletionRequest,
                    BaiChuanConvert::convert2ChatCompletionResult,
                    BaiChuanConvert::convertByResponse);
        } else {
            completions = OpenAiApiUtil.completions(getApiKey(),
                    COMPLETIONS_URL,
                    HTTP_TIMEOUT,
                    chatCompletionRequest,
                    BaiChuanConvert::convertStreamLine2ChatCompletionResult,
                    BaiChuanConvert::convertByResponse);
        }
        return completions;
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
}
