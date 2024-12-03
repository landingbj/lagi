package ai.agent.chat.wenxin;

import ai.agent.chat.BaseChatAgent;
import ai.agent.chat.wenxin.pojo.*;
import ai.common.exception.RRException;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaiduAgent extends BaseChatAgent {

    private final String url = "https://agentapi.baidu.com/assistant/getAnswer";
    private final Gson gson = new Gson();

    public BaiduAgent(AgentConfig agentConfig) {
        super(agentConfig);
    }

    private AnswerRequest convertRequest(ChatCompletionRequest data) {
        String lastMessage = ChatCompletionUtil.getLastMessage(data);
        return AnswerRequest.builder()
                .message(RequestMessage.builder().build().builder()
                        .content(RequestMessageContent.builder().type("text")
                                .value(RequestMessageValue.builder().showText(lastMessage).build())
                                .build())
                        .build())
                .source(agentConfig.getAppId())
                .from("openapi")
                .openId(StrUtil.isBlank(data.getSessionId()) ? "landingbj_default_openId" : data.getSessionId())
                .build();
    }

    private ChatCompletionResult convertResponse(AnswerResponse data) {
        if(data.getStatus() != 0) {
            log.error("baidu agent response error {}", data);
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "百度智能助手返回异常" + data.getMessage());
        }
        if(data.getData().getContent() == null || data.getData().getContent().isEmpty()) {
            log.error("baidu agent content error {}", data);
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "百度智能助手返回content异常" + data.getLogid());
        }
        ResponseDataContent responseDataContent = data.getData().getContent().get(data.getData().getContent().size() - 1);
        return ChatCompletionUtil.toChatCompletionResult(responseDataContent.getData(), null);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("appId", agentConfig.getAppId());
        queryParams.put("secretKey", agentConfig.getApiKey());
        String apiAddress = ApiInvokeUtil.buildUrlByQuery(url, queryParams);
        AnswerRequest answerRequest = convertRequest(data);
        String res = ApiInvokeUtil.post(apiAddress, null, gson.toJson(answerRequest), 30, TimeUnit.SECONDS);
        AnswerResponse answerResponse = gson.fromJson(res, AnswerResponse.class);
        return convertResponse(answerResponse);
    }

}
