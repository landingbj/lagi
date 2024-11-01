package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.GptConvert;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@LLM(modelNames = "qa")
public class LandingAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(LandingAdapter.class);
    private final Gson gson = new Gson();
    private static final int HTTP_TIMEOUT = 15 * 1000;
    private static final String API_ADDRESS = "http://ai.landingbj.com/v1/chat/completions";

    @Override
    public boolean verify() {
        if (getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return ai.utils.ApikeyUtil.isApiKeyValid(getApiKey());
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        String model = chatCompletionRequest.getModel();
        String url = API_ADDRESS;
        if (model.equals("cascade")) {
            chatCompletionRequest.setModel(null);
            chatCompletionRequest.setCategory(null);
            url = "https://lagi.saasai.top/v1/chat/completions";
        }
        LlmApiResponse completions = OpenAiApiUtil.completions(getApiKey(), url, HTTP_TIMEOUT, chatCompletionRequest,
                GptConvert::convert2ChatCompletionResult,
                GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("landing api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        String url = API_ADDRESS;
        if (model.equals("cascade")) {
            chatCompletionRequest.setModel(null);
            chatCompletionRequest.setCategory(null);
            url = "https://lagi.saasai.top/v1/chat/completions";
        }
        LlmApiResponse completions = OpenAiApiUtil.streamCompletions(getApiKey(), url, HTTP_TIMEOUT, chatCompletionRequest,
                GptConvert::convertSteamLine2ChatCompletionResult,
                GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("landing stream api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getStreamData();
    }

}
