package ai.agent.carbus;

import ai.agent.carbus.pojo.Request;
import ai.agent.carbus.pojo.Result;
import ai.common.exception.RRException;
import ai.common.utils.ObservableList;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class HangCityAgent {

    protected AgentConfig config;
    protected Map<String,  String> headers;
    protected final int MAX_RETRY_TIME = 3;

    public HangCityAgent(AgentConfig config) {
        this.config = config;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getApiKey());
        this.headers = headers;
    }

    public abstract Observable<ChatCompletionResult> chat(Request request);

    protected Map<String, Object> getOutput(String url, Request request, String errorMsg, int retryTime) {
        retryTime = Math.max(retryTime, 1);
        int tryTime = 0;
        while (tryTime < retryTime) {
            try {
                ObservableList<Result<Map<String, Object>>> sse = ApiInvokeUtil.sse(url,
                        headers, new Gson().toJson(request), 180, TimeUnit.SECONDS,
                        (a)-> new Gson().fromJson(a, new TypeToken<Result<Map<String, Object>>>(){})
                );
                List<Map<String, Object>> results = new ArrayList<>();
                sse.getObservable().blockingForEach(r->{
                    if("node_finished".equals(r.getEvent())) {
                        results.add(r.getData());
                    }
                });
                if(results.isEmpty()) {
                    throw new RRException(LLMErrorConstants.OTHER_ERROR, errorMsg);
                }
                return results.get(results.size() -1);
            } catch (Exception e) {
                log.error("getOutput error: {}", e.getMessage());
            }
            tryTime++;
        }
        throw new RRException(LLMErrorConstants.OTHER_ERROR, errorMsg);
    }


    protected ChatCompletionResult convert2StreamResult(String response) {
        Map<String, Object> out = new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
        if("node_chunk".endsWith((String) out.get("event"))) {
            Result<ChatCompletionResult> chatCompletionResultResult = new Gson().fromJson(response, new TypeToken<Result<ChatCompletionResult>>() {
            });
            Object o = out.get("session_id");
            if(o != null) {
                chatCompletionResultResult.getData().setSession_id((String) o);
            }
            chatCompletionResultResult.getData().getChoices().forEach(choice->{
                ChatMessage delta = choice.getDelta();
                choice.setMessage(delta);
                choice.setDelta(null);
            });
            return chatCompletionResultResult.getData();
        }
        // 非流式 设为 null 被过滤
        return null;
    }
}
