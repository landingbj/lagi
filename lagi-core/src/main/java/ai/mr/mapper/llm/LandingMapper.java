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

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.learning.pojo.QaPairRequest;
import ai.learning.pojo.QaPairResponse;
import ai.common.pojo.Backend;
import ai.learning.pojo.SimulatingTreeRequest;
import ai.learning.pojo.SimulatingTreeResponse;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import weixin.tools.TulingThread;

import java.util.ArrayList;
import java.util.List;

public class LandingMapper extends BaseMapper implements IMapper {
    private final Gson gson = new Gson();
    private final AiServiceCall call = new AiServiceCall();

    protected int priority;

    @Override
    public List<?> myMapping() {
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                LagiGlobal.CHAT_COMPLETION_REQUEST);
        Backend backendConfig = (Backend) this.getParameters().get(LagiGlobal.CHAT_COMPLETION_CONFIG);
        List<?> result = new ArrayList<>();
        if (backendConfig.getModel().equalsIgnoreCase(LagiGlobal.LANDING_MODEL_QA)) {
            result = qaMapping(chatCompletionRequest, backendConfig);
        } else if (backendConfig.getModel().equalsIgnoreCase(LagiGlobal.LANDING_MODEL_TREE)) {
            result = treeMapping(chatCompletionRequest, backendConfig);
        } else if (backendConfig.getModel().equalsIgnoreCase(LagiGlobal.LANDING_MODEL_TURING)) {
            result = tulingMapping(chatCompletionRequest, backendConfig);
        }
        return result;
    }

    private List<?> treeMapping(ChatCompletionRequest chatCompletionRequest, Backend backendConfig) {
        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());

        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String category = chatCompletionRequest.getCategory();

        SimulatingTreeRequest simulatingTreeRequest = new SimulatingTreeRequest();
        simulatingTreeRequest.setCategory(category);
        simulatingTreeRequest.setQuestion(question);

        Object[] params = {gson.toJson(simulatingTreeRequest)};
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

    private List<?> qaMapping(ChatCompletionRequest chatCompletionRequest, Backend backendConfig) {
        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());

        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String category = chatCompletionRequest.getCategory();

        QaPairRequest qaPairRequest = new QaPairRequest();
        qaPairRequest.setCategory(category);
        qaPairRequest.setQuestion(question);

        Object[] params = {gson.toJson(qaPairRequest)};
        String qaPairResult = call.callWS(AiServiceInfo.WSKngUrl, "qaPair", params)[0];
        QaPairResponse answerPair = gson.fromJson(qaPairResult, QaPairResponse.class);
        List<Object> result = new ArrayList<>();

        String textResult = null;

        if (answerPair != null) {
            textResult = answerPair.getAnswer();
        }

        ChatCompletionResult chatCompletionResult = ChatCompletionUtil.toChatCompletionResult(textResult, backendConfig.getModel());

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority());

        return result;
    }

    private List<?> tulingMapping(ChatCompletionRequest chatCompletionRequest, Backend backendConfig) {
        List<Object> result = new ArrayList<>();
        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());

        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);

        String textResult = handleTuling(question);

        ChatCompletionResult chatCompletionResult = ChatCompletionUtil.toChatCompletionResult(textResult, backendConfig.getModel());

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority());
        return result;
    }

    private String handleTuling(String reqMessage) {
        String result = null;
        String respMessage = TulingThread.lookupKeywords(reqMessage);
        if (respMessage != null)
            result = respMessage;

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
