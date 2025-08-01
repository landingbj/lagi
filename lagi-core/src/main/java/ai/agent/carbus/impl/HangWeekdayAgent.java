package ai.agent.carbus.impl;

import ai.agent.carbus.HangCityAgent;
import ai.agent.carbus.pojo.*;
import ai.agent.carbus.util.ApiForCarBus;
import ai.agent.carbus.util.RouteProcessor;
import ai.common.exception.RRException;
import ai.common.utils.ObservableList;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.ApiInvokeUtil;
import ai.utils.DelayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import org.apache.hadoop.util.Lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HangWeekdayAgent extends HangCityAgent {

    private final String baseApiUrl = "http://20.17.127.24:11105/aicoapi/gateway/v2/chatbot/api_run/";

    private final String toolInvokeAppId = "1753063832_d3745079-d17c-4a4e-bf65-6f4db13be7ab";
    private final String answerAppId = "1753063785_720be409-2c96-4b21-8ea8-a1c25c726078";
    private final int radius4BicycleStation = 300;

    public HangWeekdayAgent(AgentConfig config) {
        super(config);
    }


    public Observable<ChatCompletionResult> chat(Request request) {
        return null;
    }

}
