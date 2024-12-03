package ai.agent.chat.qianfan;

import ai.agent.chat.BaseChatAgent;
import ai.agent.pojo.XiaoxinRequest;
import ai.agent.pojo.XiaoxinResponse;
import ai.common.exception.RRException;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.OkHttpUtil;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XiaoxinAgent extends BaseChatAgent {
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(XiaoxinAgent.class);
    private static final String BASE_URL = "https://api-ngd.baidu.com/core/v3/query";
    private static final Logger log = LoggerFactory.getLogger(XiaoxinAgent.class);


    public XiaoxinAgent(AgentConfig agentConfig) {
        super(agentConfig);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest request)  {
        XiaoxinRequest xiaoxinRequest = convertRequest(request);
        String json = gson.toJson(xiaoxinRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", agentConfig.getToken());
        Map<String, String> params = new HashMap<>();
        params.put("debug", "false");
        params.put("nlu", "false");
        try {
            String responseJson = OkHttpUtil.post(BASE_URL, headers, params, json);
            log.info("xiaoxin response: " + responseJson);
            XiaoxinResponse response = gson.fromJson(responseJson, XiaoxinResponse.class);
            return convertResult(response);
        } catch (IOException e) {
            logger.error("xiaoxin error", e);
        }
        throw new RRException(LLMErrorConstants.OTHER_ERROR, "xiaoxin error");
    }

    private XiaoxinRequest convertRequest(ChatCompletionRequest request) {
        String queryText = ChatCompletionUtil.getLastMessage(request);
        XiaoxinRequest xiaoxinRequest = XiaoxinRequest.builder()
                .queryText(queryText)
                .sessionId(request.getSessionId())
                .build();
        return xiaoxinRequest;
    }

    private ChatCompletionResult convertResult(XiaoxinResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getCode() != 200) {
            throw new RuntimeException("Xiaoxin error message: " + response.getCode() + " " + response.getMsg());
        }
        XiaoxinResponse.Answer answer = response.getData().getAnswer();
        if (answer == null || answer.getAnswerText() == null) {
            return null;
        }
        ChatCompletionResult result = ChatCompletionUtil.toChatCompletionResult(answer.getAnswerText(), null);
        return result;
    }
}
