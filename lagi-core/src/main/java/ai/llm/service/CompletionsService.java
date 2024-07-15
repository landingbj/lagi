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
import ai.llm.utils.CacheManager;
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
    private static final int DEFAULT_MAX_TOKENS = 1024;

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
                    ModelService modelService = (ModelService) adapter;
                    if(!CacheManager.get(modelService.getModel())) {
                        continue;
                    }
                    try {
                        Observable<ChatCompletionResult> chatCompletionResultObservable = adapter.streamCompletions(chatCompletionRequest);
//                        chatCompletionResultObservable.subscribe(v->{},e->{
//                            CacheManager.put(modelService.getModel(), Boolean.FALSE);
//                            System.out.println("模型报错  已未您切换");
//                        });
                        return chatCompletionResultObservable;
                    }catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
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

    public ChatMessage getChatMessage(String question, String role) {
        ChatMessage message = new ChatMessage();
        message.setRole(role);
        message.setContent(question);
        return message;
    }

    public ChatCompletionRequest getCompletionsRequest(String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(getChatMessage(prompt, LagiGlobal.LLM_ROLE_USER));
        return getCompletionsRequest(messages);
    }

    public ChatCompletionRequest getCompletionsRequest(String systemPrompt, String prompt, String category) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null) {
            messages.add(getChatMessage(systemPrompt, LagiGlobal.LLM_ROLE_SYSTEM));
        }
        messages.add(getChatMessage(prompt, LagiGlobal.LLM_ROLE_USER));
        return getCompletionsRequest(messages, category);
    }

    public ChatCompletionRequest getCompletionsRequest(List<ChatMessage> messages) {
        return getCompletionsRequest(messages, null);
    }

    public ChatCompletionRequest getCompletionsRequest(List<ChatMessage> messages, String category) {
        return getCompletionsRequest(messages, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS, category);
    }

    public ChatCompletionRequest getCompletionsRequest(List<ChatMessage> messages, double temperature, int maxTokens, String category) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(temperature);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(maxTokens);
        chatCompletionRequest.setMessages(messages);
        chatCompletionRequest.setCategory(category);
        return chatCompletionRequest;
    }
}
