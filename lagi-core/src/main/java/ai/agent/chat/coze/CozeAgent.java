package ai.agent.chat.coze;

import ai.agent.chat.BaseChatAgent;
import ai.agent.pojo.*;
import ai.common.exception.RRException;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.qa.ChatCompletionUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class CozeAgent extends BaseChatAgent {
    private static final Logger logger = LoggerFactory.getLogger(CozeAgent.class);
    private static final Gson gson = new Gson();
    private static final String CHAT_URL = "https://api.coze.cn/v3/chat";
    private static final String QUERY_STATUS_URL = "https://api.coze.cn/v3/chat/retrieve";
    private static final String QUERY_RESULT_URL = "https://api.coze.cn/v3/chat/message/list";
    private final Integer maxLoopTimes = 120;


    public CozeAgent(AgentConfig agentConfig) {
        super(agentConfig);
    }


    public CozeChatResponse<CozeChatObject> createNewChat(Map<String, String> queryParams, Map<String, String> headers, String body) {
        String responseJson = ApiInvokeUtil.post(ApiInvokeUtil.buildUrlByQuery(CHAT_URL, queryParams), headers,  body, 30, TimeUnit.SECONDS);
        Type responseType = new TypeToken<CozeChatResponse<CozeChatObject>>() {}.getType();
        return gson.fromJson(responseJson, responseType);
    }

    public CozeChatResponse<CozeChatObject> queryStatus(Map<String, String> queryParams, Map<String, String> headers)  {
        String s = ApiInvokeUtil.get(QUERY_STATUS_URL, queryParams, headers, 30, TimeUnit.SECONDS);
        Type responseType = new TypeToken<CozeChatResponse<CozeChatObject>>() {}.getType();
        return gson.fromJson(s, responseType);
    }

    public CozeChatResponse<List<CozeChatResultData>> queryResult(Map<String, String> queryParams, Map<String, String> headers) {
        String s = ApiInvokeUtil.get(QUERY_RESULT_URL, queryParams, headers, 30, TimeUnit.SECONDS);
        Type responseType = new TypeToken<CozeChatResponse<List<CozeChatResultData>>>() {}.getType();
        return gson.fromJson(s, responseType);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest request) {
        CozeChatRequest chatRequest = convertRequest(request, agentConfig);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + this.agentConfig.getToken());
        String body = gson.toJson(chatRequest);
        CozeChatResponse<CozeChatObject> newChat = createNewChat(null, headers, body);
        if(newChat == null) {
            return null;
        }
        if(newChat.getCode() != 0) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, newChat.getMsg());
        }
        String conversationId = newChat.getData().getConversation_id();
        String chatId = newChat.getData().getId();
        Map<String, String> params = new HashMap<>();
        params.put("chat_id", chatId);
        params.put("conversation_id", conversationId);
        int count = maxLoopTimes;
        while (count-- > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            CozeChatResponse<CozeChatObject> cozeChatObject = queryStatus(params, headers);
            if(cozeChatObject == null) {
                throw new RRException(LLMErrorConstants.OTHER_ERROR, "not response");
            }
            if(cozeChatObject.getCode() != 0) {
                throw new RRException(LLMErrorConstants.OTHER_ERROR, cozeChatObject.getMsg());
            }
            CozeChatObject data = cozeChatObject.getData();
            if("created".equals(data.getStatus())
                    || "in_progress".equals(data.getStatus())) {
                continue;
            }
            if("completed".equals(data.getStatus())) {
                break;
            }
            throw new RRException(LLMErrorConstants.OTHER_ERROR, cozeChatObject.getMsg());
        }
        CozeChatResponse<List<CozeChatResultData>> resultResponse = queryResult(params, headers);
        if(0 != resultResponse.getCode()) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, resultResponse.getMsg());
        }

        return convertResult(resultResponse);
    }

    private CozeChatRequest convertRequest(ChatCompletionRequest request, AgentConfig agentConfig) {

        String queryText = ChatCompletionUtil.getLastMessage(request);
        CozeAdditionalMessages additionalMessage = CozeAdditionalMessages.builder()
                .role("user")
                .content_type("text")
                .content(queryText)
                .build();

        return CozeChatRequest.builder()
                .bot_id(agentConfig.getAppId())
                .user_id(agentConfig.getUserId() == null ? "1" : agentConfig.getUserId())
                .stream(request.getStream())
                .auto_save_history(Boolean.FALSE.equals(request.getStream()))
                .additional_messages((Lists.newArrayList(additionalMessage)))
                .build();
    }



    private ChatCompletionResult convertResult(CozeChatResponse<List<CozeChatResultData>> response) {
        List<String> answers = response.getData().stream()
                .filter(data -> "answer".equals(data.getType()))
                .map(CozeChatResultData::getContent)
                .collect(Collectors.toList());
        String answer = String.join("\n", answers);
        return ChatCompletionUtil.toChatCompletionResult(answer, null);
    }

}
