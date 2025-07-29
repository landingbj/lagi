package ai.agent.carbus;

import ai.agent.carbus.pojo.Request;
import ai.agent.carbus.pojo.Result;
import ai.common.exception.RRException;
import ai.common.utils.ObservableList;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class HangCityAgent {

    protected AgentConfig config;
    protected Map<String,  String> headers;

    public HangCityAgent(AgentConfig config) {
        this.config = config;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getApiKey());
        this.headers = headers;
    }

    protected abstract Observable<ChatCompletionResult> chat(Request request);

    protected Map<String, Object> getOutput(String url, Request request, String errorMsg) {
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
        return results.get(0);
    }

}
