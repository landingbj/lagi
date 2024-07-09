/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

package ai.llm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.common.ModelService;
import ai.common.pojo.IndexSearchData;
import ai.llm.adapter.ILlmAdapter;
import ai.common.pojo.Backend;
import ai.manager.LlmManager;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.FastDirectContainer;
import ai.mr.mapper.llm.*;
import ai.mr.reducer.llm.QaReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.SensitiveWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.bean.BeanUtil;
import io.reactivex.Observable;
import weixin.tools.TulingThread;

public class CompletionsService {
    private static TulingThread tulingProcessor = null;
    private static final double DEFAULT_TEMPERATURE = 0.8;
    private static final int DEFAULT_MAX_TOKENS = 2048;

    static {
        if (tulingProcessor == null) {
            tulingProcessor = new TulingThread();
            tulingProcessor.setDaemon(true);
            tulingProcessor.start();
        }
    }


    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        ChatCompletionResult answer = null;
        try (IRContainer contain = new FastDirectContainer()) {
            if (chatCompletionRequest.getModel() != null) {
                LlmManager.getInstance().getAdapters().stream().filter(adapter -> {
                    if (adapter instanceof ModelService) {
                        ModelService modelService = (ModelService) adapter;
                        return modelService.getModel().equals(chatCompletionRequest.getModel());
                    }
                    return false;
                }).findAny().ifPresent(adapter -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
                    Backend backend = new Backend();
                    BeanUtil.copyProperties((ModelService) adapter, backend);
                    params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
                    IMapper mapper = getMapper(backend);
                    mapper.setParameters(params);
                    mapper.setPriority(backend.getPriority());
                    contain.registerMapper(mapper);
                });
            } else {
                for (ILlmAdapter adapter : LlmManager.getInstance().getAdapters()) {
                    if (adapter instanceof ModelService) {
                        ModelService modelService = (ModelService) adapter;
                        Map<String, Object> params = new HashMap<>();
                        params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
                        Backend backend = new Backend();
                        BeanUtil.copyProperties(modelService, backend);
                        params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
                        IMapper mapper = getMapper(backend);
                        mapper.setParameters(params);
                        mapper.setPriority(backend.getPriority());
                        contain.registerMapper(mapper);
                    }
                }
            }
            IReducer qaReducer = new QaReducer();
            contain.registerReducer(qaReducer);

            @SuppressWarnings("unchecked")
            List<ChatCompletionResult> resultMatrix = (List<ChatCompletionResult>) contain.Init().running();
            if (resultMatrix.get(0) != null) {
                answer = resultMatrix.get(0);
                if (SensitiveWordUtil.containSensitiveWord(answer)) {
                    return null;
                }
            }
        }

        return answer;
    }

    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {

        if (chatCompletionRequest.getModel() != null) {
            ILlmAdapter adapter = LlmManager.getInstance().getAdapter(chatCompletionRequest.getModel());
            if (adapter != null) {
                return adapter.streamCompletions(chatCompletionRequest);
            }
        } else {
            for (ILlmAdapter adapter : getAdapters()) {
                if (adapter != null) {
                    return adapter.streamCompletions(chatCompletionRequest);
                }
            }
        }
        throw new RuntimeException("Stream backend is not enabled.");
    }

    private List<ILlmAdapter> getAdapters() {
        return LlmManager.getInstance().getAdapters();
    }

    private ILlmAdapter getAdapter(Backend backendConfig) {
        return LlmManager.getInstance().getAdapter(backendConfig.getModel());
    }


    private IMapper getMapper(Backend backendConfig) {
        if (backendConfig.getType().equalsIgnoreCase(LagiGlobal.LLM_TYPE_LANDING)) {
            return new LandingMapper();
        }
        return new UniversalMapper(getAdapter(backendConfig));
    }


    public void addVectorDBContext(ChatCompletionRequest request, String context) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        String prompt = "以下是背景信息。\\n---------------------\\n%s\\n---------------------\\n根据上下文信息而非先前知识，回答这个问题:%s\\n";
        prompt = String.format(prompt, context, lastMessage);
        ChatCompletionUtil.setLastMessage(request, prompt);
    }

    public String getRagContext(List<IndexSearchData> indexSearchDataList) {
        if (indexSearchDataList.isEmpty()) {
            return null;
        }
        String context = indexSearchDataList.get(0).getText();
        double firstDistance = indexSearchDataList.get(0).getDistance();
        double lastDistance = firstDistance;
        List<Double> diffList = new ArrayList<>();
        for (int i = 1;i < indexSearchDataList.size();i ++) {
            if (i == 1) {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - firstDistance;
                double threshold = diff / firstDistance;
                if (threshold < 0.25) {
                    context += "\n" + data.getText();
                    lastDistance = data.getDistance();
                    diffList.add(diff);
                } else {
                    break;
                }
            } else if (i == 2) {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - lastDistance;
                double threshold = diff / diffList.get(0);
                if (diff < diffList.get(0) * 0.618) {
                    context += "\n" + data.getText();
                    lastDistance = data.getDistance();
                    diffList.add(diff);
                } else {
                    break;
                }
            } else {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - lastDistance;
                double average = diffList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                System.out.println(i + " diff = " + diff + " average = " + average);
                if (diff < average) {
                    context += "\n" + data.getText();
                    lastDistance = data.getDistance();
                    diffList.add(diff);
                } else {
                    break;
                }
            }
        }
        return context;
    }

    public ChatCompletionRequest getCompletionsRequest(String prompt) {
        return getCompletionsRequest(prompt, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS);
    }

    public ChatCompletionRequest getCompletionsRequest(String prompt, double temperature, int maxTokens) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(temperature);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(maxTokens);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }
}
