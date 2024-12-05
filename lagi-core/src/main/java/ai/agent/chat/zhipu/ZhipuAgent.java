package ai.agent.chat.zhipu;

import ai.agent.chat.BaseChatAgent;
import ai.agent.chat.zhipu.pojo.DataJson;
import ai.agent.chat.zhipu.pojo.Response;
import ai.agent.chat.zhipu.pojo.GenRequest;
import ai.agent.chat.zhipu.pojo.KeyValuePairs;
import ai.common.exception.RRException;
import ai.common.utils.ObservableList;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZhipuAgent extends BaseChatAgent {

    private Gson gson = new Gson();

    private final String CREATE_CONVERSATION_URL_PATTERN = "https://open.bigmodel.cn/api/llm-application/open/v2/application/{}/conversation";
    private final String GENERATE_REQUEST_URL = "https://open.bigmodel.cn/api/llm-application/open/v2/application/generate_request_id";
    private final String QUERY_RESULT_URL_PATTERN = "https://open.bigmodel.cn/api/llm-application/open/v2/model-api/{}/sse-invoke";

    public ZhipuAgent(AgentConfig agentConfig) {
        super(agentConfig);
    }

    private Response createConversation() {
        String url = StrUtil.format(CREATE_CONVERSATION_URL_PATTERN, agentConfig.getAppId());
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + agentConfig.getToken());
        String post = ApiInvokeUtil.post(url, headers, "", 15, TimeUnit.SECONDS);
        log.info("botId: {}, [zhipu] agent create conversation result\n {}", agentConfig.getAppId(), post);
        return gson.fromJson(post, Response.class);
    }

    private Response genRequestId(String conversationId, ChatCompletionRequest data) {
        String question = ChatCompletionUtil.getLastMessage(data);
        KeyValuePairs build = KeyValuePairs.builder().id("user").type("input").name("用户提问").value(question).build();
        List<KeyValuePairs> keyValuePairs = Lists.newArrayList(build);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + agentConfig.getToken());
        GenRequest request = GenRequest.builder().app_id(agentConfig.getAppId())
                .conversation_id(conversationId)
                .key_value_pairs(keyValuePairs)
                .build();
        String post = ApiInvokeUtil.post(GENERATE_REQUEST_URL, headers, gson.toJson(request), 15, TimeUnit.SECONDS);
        log.info("botId: {}, [zhipu] agent genRequestId result\n {}", agentConfig.getAppId(), post);
        return gson.fromJson(post, Response.class);
    }

    private ObservableList<ChatCompletionResult> QueryResult(String requestId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " +agentConfig.getToken());
        String url = StrUtil.format(QUERY_RESULT_URL_PATTERN, requestId);
        ObservableList<ChatCompletionResult> res = ApiInvokeUtil.sse(url, headers, "", 15, TimeUnit.SECONDS, s -> {
            DataJson dataJson = gson.fromJson(s, DataJson.class);
            String msg = "";
            if(dataJson != null && dataJson.getMsg() != null) {
                msg = dataJson.getMsg();
            }
            String format = String.format("{\"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"%s\"}}]}", msg);
            return gson.fromJson(format, ChatCompletionResult.class);
        });
        return res;
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        Response conversation = createConversation();
        if(conversation.getCode() != 200) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "zhipu create conversation error");
        }
        Response requestIdResponse = genRequestId(conversation.getData().getConversation_id(), data);
        if(requestIdResponse.getCode() != 200) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "zhipu create request id  error");
        }
        ObservableList<ChatCompletionResult> result = QueryResult(requestIdResponse.getData().getId());
        String format = String.format("{\"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"\"}}]}");
        ChatCompletionResult chatCompletionResult = gson.fromJson(format, ChatCompletionResult.class);
        StringBuffer sb = new StringBuffer();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        result.getObservable().subscribe(c -> {
            String content = c.getChoices().get(c.getChoices().size() - 1).getMessage().getContent();
            sb.append(content);
        }, e -> {countDownLatch.await();}, countDownLatch::countDown);
        try {
            countDownLatch.await();
        } catch (Exception e) {

        }
        chatCompletionResult.getChoices().get(0).getMessage().setContent(sb.toString());
        return chatCompletionResult;
    }
}
