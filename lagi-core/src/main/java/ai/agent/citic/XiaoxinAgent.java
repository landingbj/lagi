package ai.agent.citic;

import ai.agent.pojo.XiaoxinRequest;
import ai.agent.pojo.XiaoxinResponse;
import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LRUCache;
import ai.utils.OkHttpUtil;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XiaoxinAgent {
    private static final Gson gson = new Gson();
    private static final String BASE_URL = "https://api-ngd.baidu.com/core/v3/query";
    private final AgentConfig agentConfig;

    public XiaoxinAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    public ChatCompletionResult chat(ChatCompletionRequest request) throws IOException {
        XiaoxinRequest xiaoxinRequest = convertRequest(request);
        String json = gson.toJson(xiaoxinRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", agentConfig.getToken());
        Map<String, String> params = new HashMap<>();
        params.put("debug", "false");
        params.put("nlu", "false");
        String responseJson = OkHttpUtil.post(BASE_URL, headers, params, json);
        XiaoxinResponse response = gson.fromJson(responseJson, XiaoxinResponse.class);
        return convertResult(response);
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
