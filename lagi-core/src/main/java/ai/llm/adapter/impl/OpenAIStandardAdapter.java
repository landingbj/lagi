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
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@LLM(modelNames = {"*"})
public class OpenAIStandardAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIStandardAdapter.class);
    private static final int HTTP_TIMEOUT = 30 * 1000;

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        LlmApiResponse completions = OpenAiApiUtil.completions(apiKey, getApiAddress(), HTTP_TIMEOUT, chatCompletionRequest,
                GptConvert::convert2ChatCompletionResult, GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("openai api : code{}  error  {}", completions.getCode(), completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
    }



    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        LlmApiResponse completions = OpenAiApiUtil.streamCompletions(apiKey, getApiAddress(), HTTP_TIMEOUT, chatCompletionRequest,
                GptConvert::convertSteamLine2ChatCompletionResult, GptConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("openai  stream api : code{}  error  {}", completions.getCode(), completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getStreamData();
    }

    private void setDefaultModel(ChatCompletionRequest request) {
        try {
            String url = "http://127.0.0.1:8800/v1/models";
            String response = HttpUtil.get(url);
            List<String> llmIds = extractLlmIds(response);
                request.setModel(llmIds.get(0));
        }catch (Exception e){
            if (request.getModel() == null) {
                request.setModel(getModel());
                System.out.println("8800未找到对应的模型 llmIds:");
            }
        }

    }
    private static List<String> extractLlmIds(String jsonResponse) {
        List<String> llmIds = new ArrayList<>();
        if (JSONUtil.isJson(jsonResponse)) {
            JSONObject jsonObject = JSONUtil.parseObj(jsonResponse);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray != null) {
                for (Object obj : dataArray) {
                    JSONObject dataObject = (JSONObject) obj;
                    String modelType = dataObject.getStr("model_type");
                    if ("LLM".equals(modelType)) {
                        String id = dataObject.getStr("id");
                        llmIds.add(id);
                    }
                }
            }
        }
        return llmIds;
    }
}
