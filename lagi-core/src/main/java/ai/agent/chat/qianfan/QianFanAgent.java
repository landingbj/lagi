package ai.agent.chat.qianfan;

import ai.agent.chat.BaseChatAgent;
import ai.agent.pojo.*;
import ai.common.exception.RRException;
import ai.common.utils.LRUCache;
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
import java.util.concurrent.TimeUnit;

public class QianFanAgent extends BaseChatAgent {
    private static final Logger logger = LoggerFactory.getLogger(StockAgent.class);
    private static final Gson gson = new Gson();
    private static final String BASE_URL = "https://qianfan.baidubce.com/v2/app/conversation/runs";
    private static final String NEW_CONVERSATION_URL = "https://qianfan.baidubce.com/v2/app/conversation";
    private static final LRUCache<String, String> sessionCache = new LRUCache<>(1000, 5, TimeUnit.DAYS);

    public QianFanAgent(AgentConfig agentConfig) {
        super(agentConfig);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest request)  {
        StockRequest stockRequest = convertRequest(request, this.agentConfig);
        String json = gson.toJson(stockRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Appbuilder-Authorization", "Bearer " + this.agentConfig.getToken());
        try {
            String responseJson = OkHttpUtil.post(BASE_URL, headers, new HashMap<>(), json);
            System.out.println(responseJson);
            StockResponse response = gson.fromJson(responseJson, StockResponse.class);
            return convertResult(response);
        } catch (Exception e) {
            logger.error("exchange error", e);
        }
        throw new RRException(LLMErrorConstants.OTHER_ERROR, "exchange error");
    }

    private StockRequest convertRequest(ChatCompletionRequest request, AgentConfig agentConfig) {
//        String queryText = String.format("用大约 50 个字概述用三引号分隔的问题:\"\"\"%s\"\"\"", ChatCompletionUtil.getLastMessage(request));
        String queryText = ChatCompletionUtil.getLastMessage(request);
        StockRequest stockRequest = StockRequest.builder()
                .stream(request.getStream())
                .app_id(agentConfig.getAppId())
                .query(queryText)
                .conversation_id(getConversationId(request.getSessionId()))
                .build();
        return stockRequest;
    }

    private String getConversationId(String sessionId) {
        String conversationId = sessionCache.get(sessionId);
        if (conversationId == null) {
            conversationId = createNewConversation();
            sessionCache.put(sessionId, conversationId);
        }
        return conversationId;
    }

    private String createNewConversation() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Appbuilder-Authorization", "Bearer " + agentConfig.getToken());
        String responseJson = null;
        NewConversationRequest newConversationRequest = NewConversationRequest.builder().app_id(agentConfig.getAppId()).build();
        try {
            responseJson = OkHttpUtil.post(NEW_CONVERSATION_URL, headers, new HashMap<>(), gson.toJson(newConversationRequest));
        } catch (IOException e) {
            logger.error("create new conversation error", e);
        }
        NewConversationResponse response = gson.fromJson(responseJson, NewConversationResponse.class);
        if (response.getCode() != null) {
            throw new RuntimeException("create new conversation error message: " + response.getCode() + " " + response.getMessage());
        }
        return response.getConversation_id();
    }

    private ChatCompletionResult convertResult(StockResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getCode() != null) {
            throw new RuntimeException("exchange error message: " + response.getCode() + " " + response.getMessage());
        }
        String answer = response.getAnswer();
        ChatCompletionResult result = ChatCompletionUtil.toChatCompletionResult(answer, null);
        return result;
    }
}
