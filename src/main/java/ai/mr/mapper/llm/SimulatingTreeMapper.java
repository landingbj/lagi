/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * RepDao_Mapper.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr.mapper.llm;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.learning.pojo.SimulatingTreeRequest;
import ai.learning.pojo.SimulatingTreeResponse;
import ai.migrate.pojo.Backend;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;

public class SimulatingTreeMapper extends BaseMapper implements IMapper {

    private static boolean _DEBUG_1 = false;
    private static boolean _DEBUG_2 = false;
    private static boolean _DEBUG_3 = false;
    private static boolean _DEBUG_4 = false;
    private static boolean _DEBUG_5 = false;

    static {
        if (AiGlobalQA._DEBUG_LEVEL >= 5) {
            _DEBUG_5 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 4) {
            _DEBUG_4 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 3) {
            _DEBUG_3 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 2) {
            _DEBUG_2 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 1) {
            _DEBUG_1 = true;
        }
    }
    protected int priority;

    private static Gson gson = new Gson();
    private static AiServiceCall call = new AiServiceCall();

    @Override
    public List<?> myMapping() {
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                LagiGlobal.CHAT_COMPLETION_REQUEST);
        Backend backendConfig = (Backend) this.getParameters().get(LagiGlobal.CHAT_COMPLETION_CONFIG);
        
        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());
        
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String category = chatCompletionRequest.getCategory();

        SimulatingTreeRequest simulatingTreeRequest = new SimulatingTreeRequest();
        simulatingTreeRequest.setCategory(category);
        simulatingTreeRequest.setQuestion(question);

        Object[] params = { gson.toJson(simulatingTreeRequest) };
        String simulatingTreeResult = call.callWS(AiServiceInfo.WSKngUrl, "simulatingTree", params)[0];
        SimulatingTreeResponse simulatingTreeResponse = gson.fromJson(simulatingTreeResult,
                SimulatingTreeResponse.class);

        String textResult = null;
        if (simulatingTreeResponse != null) {
            textResult = simulatingTreeResponse.getAnswer();
        }
        List<Object> result = new ArrayList<>();
        
        ChatCompletionResult chatCompletionResult = ChatCompletionUtil.toChatCompletionResult(textResult, backendConfig.getModel());

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(getPriority());

        return result;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
