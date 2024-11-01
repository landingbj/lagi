package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;;
import ai.llm.utils.convert.GptAzureConvert;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@LLM(modelNames = {"gpt-3.5-turbo","gpt-4-1106-preview","gpt-4o-20240513"})
public class GPTAzureAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GPTAzureAdapter.class);
    private final Gson gson = new Gson();
    private static final String ENTERPOINT = "https://api.openai.com/v1/chat/completions";
    private static final int HTTP_TIMEOUT = 30 * 1000;



    @Override
    public String getApiAddress() {
        return getEndpoint() + "openai/deployments/" + getDeployment() + "/chat/completions?api-version=" + getApiVersion();
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        chatCompletionRequest.setCategory(null);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("api-key", getApiKey());

        LlmApiResponse completions = OpenAiApiUtil.completions(getApiKey(), getApiAddress(), HTTP_TIMEOUT, chatCompletionRequest,
                GptAzureConvert::convert2ChatCompletionResult, GptAzureConvert::convertByResponse,
                headers);
        if(completions.getCode() != 200) {
            logger.error("open ai  api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        chatCompletionRequest.setCategory(null);
        String apiUrl = getApiAddress();
        String apiKey = getApiKey();
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", apiKey);
        LlmApiResponse llmApiResponse = OpenAiApiUtil.streamCompletions(apiKey, apiUrl, HTTP_TIMEOUT, chatCompletionRequest,
                GptAzureConvert::convertStreamLine2ChatCompletionResult, GptAzureConvert::convertByResponse, headers);
        Integer code = llmApiResponse.getCode();
        if(code != 200) {
            logger.error("open ai stream api error {}", llmApiResponse.getMsg());
            throw new RRException(code, llmApiResponse.getMsg());
        }
        return llmApiResponse.getStreamData();
    }


    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
}
