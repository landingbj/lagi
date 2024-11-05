/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

package ai.llm.service;

import java.util.*;
import java.util.stream.Collectors;

import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.config.pojo.Policy;
import ai.llm.adapter.ILlmAdapter;
import ai.common.pojo.Backend;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.pojo.GetRagContext;
import ai.llm.utils.CacheManager;
import ai.llm.utils.LLMErrorConstants;
import ai.llm.utils.PolicyConstants;
import ai.llm.utils.PollingScheduler;
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
import lombok.extern.slf4j.Slf4j;
import weixin.tools.TulingThread;

@Slf4j
public class CompletionsService implements ChatCompletion{
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

    private Policy getPolicy() {
        return ContextLoader.configuration.getFunctions().getPolicy();
    }

    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        // The execution model is specified
        RRException r = new RRException(LLMErrorConstants.OTHER_ERROR,"{\"error\":\"backend is not enabled.\"}");
        if (chatCompletionRequest.getModel() != null) {
            ILlmAdapter appointAdapter = LlmManager.getInstance().getAdapter(chatCompletionRequest.getModel());
            if(appointAdapter != null && notFreezingAdapter(appointAdapter)) {
                try {
                    ChatCompletionResult result = SensitiveWordUtil.filter(appointAdapter.completions(chatCompletionRequest));
                    unfreezeAdapter(appointAdapter);
                    return result;
                } catch (RRException e) {
                    freezingAdapterByErrorCode(appointAdapter, e.getCode());
                    r = e;
                }
            }
        }
        List<ILlmAdapter> adapters = getLlmAdapters(indexSearchDataList);
        //  Operation strategy : failover or parallel or other
        String handle = getPolicy().getHandle();
        if(PolicyConstants.FAILOVER.equals(handle)) {
            return failoverGetChatCompletionResult(chatCompletionRequest, adapters);
        } else if(PolicyConstants.PARALLEL.equals(handle)) {
            return parallelGetChatCompletionResult(chatCompletionRequest, adapters);
        } else if(PolicyConstants.POLLING.equals(handle)){
            return pollingGetChatCompletionResult(chatCompletionRequest, adapters);
        }
        throw r;
    }

    public ChatCompletionResult failoverGetChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        chatCompletionRequest.setModel(null);
        RRException r = new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL,"{\"error\":\"failover -> backend is not enabled.\"}");
        for (ILlmAdapter adapter : ragAdapters) {
            ChatCompletionRequest copy = new ChatCompletionRequest();
            BeanUtil.copyProperties(chatCompletionRequest, copy);
            try {
                ChatCompletionResult result = SensitiveWordUtil.filter(adapter.completions(copy));
                unfreezeAdapter(adapter);
                return result;
            } catch (RRException e) {
                freezingAdapterByErrorCode(adapter, e.getCode());
                r = e;
            }
        }
        throw  r;
    }



    private ChatCompletionResult parallelGetChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        chatCompletionRequest.setModel(null);
        ChatCompletionResult answer;
        try (FastDirectContainer contain = new FastDirectContainer() {
            @Override
            public void onMapperFail(String mapperName, Integer priority, Throwable throwable) {
                super.onMapperFail(mapperName, priority, throwable);

                IMapper iMapper = mappersGroup.get(mapperName);
                RRException exception;
                if(!(throwable instanceof RRException)) {
                    return;
                }
                exception = (RRException) throwable;
                if(!(iMapper instanceof  UniversalMapper)) {
                    return;
                }
                UniversalMapper universalMapper = (UniversalMapper) iMapper;
                ILlmAdapter adapter = universalMapper.getAdapter();
                freezingAdapterByErrorCode(adapter, exception.getCode());
            }
        }) {
            for (ILlmAdapter adapter : ragAdapters) {
                ChatCompletionRequest copy = new ChatCompletionRequest();
                BeanUtil.copyProperties(chatCompletionRequest, copy);
                registerMapper(copy, adapter, contain);
            }
            IReducer qaReducer = new QaReducer();
            contain.registerReducer(qaReducer);
            @SuppressWarnings("unchecked")
            List<ChatCompletionResult> resultMatrix = (List<ChatCompletionResult>) contain.Init().running();
            if (resultMatrix.get(0) != null) {
                answer = resultMatrix.get(0);
                answer = SensitiveWordUtil.filter(answer);
            } else {
                throw contain.getException();
            }
            return answer;
        }
    }

    private ChatCompletionResult pollingGetChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        chatCompletionRequest.setModel(null);
        RRException r = new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL,"{\"error\":\"failover -> backend is not enabled.\"}");
        // no ipaddress use sample polling
        if(chatCompletionRequest instanceof EnhanceChatCompletionRequest) {
            EnhanceChatCompletionRequest enhanceChatCompletionRequest =  (EnhanceChatCompletionRequest) chatCompletionRequest;
            int hash = enhanceChatCompletionRequest.getIp().hashCode();
            while (!ragAdapters.isEmpty()) {
                int index = Math.abs(hash) % ragAdapters.size();
                ILlmAdapter adapter = ragAdapters.get(index);
                if(adapter != null && notFreezingAdapter(adapter)) {
                    ChatCompletionRequest copy = new ChatCompletionRequest();
                    BeanUtil.copyProperties(chatCompletionRequest, copy);
                    try {
                        ChatCompletionResult result = SensitiveWordUtil.filter(adapter.completions(copy));
                        unfreezeAdapter(adapter);
                        return result;
                    } catch (RRException e) {
                        freezingAdapterByErrorCode(adapter, e.getCode());
                        r = e;
                    }
                }
                ragAdapters.remove(index);
            }
        } else {
            List<String> models = ragAdapters.stream().map(adapter -> {
                ModelService modelService = (ModelService) adapter;
                return modelService.getModel();
            }).collect(Collectors.toList());
            while (!models.isEmpty()) {
                String model = PollingScheduler.schedule(models);
                ILlmAdapter appointAdapter = LlmManager.getInstance().getAdapter(model);
                if(appointAdapter != null && notFreezingAdapter(appointAdapter)) {
                    ChatCompletionRequest copy = new ChatCompletionRequest();
                    BeanUtil.copyProperties(chatCompletionRequest, copy);
                    try {
                        ChatCompletionResult result = SensitiveWordUtil.filter(appointAdapter.completions(copy));
                        unfreezeAdapter(appointAdapter);
                        return result;
                    } catch (RRException e) {
                        freezingAdapterByErrorCode(appointAdapter, e.getCode());
                        r = e;
                    }
                }
                models.remove(model);
            }
        }
        throw  r;
    }

    private void registerMapper(ChatCompletionRequest chatCompletionRequest, ILlmAdapter adapter, IRContainer contain) {
        Map<String, Object> params = new HashMap<>();
        params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
        Backend backend = new Backend();
        backend.setModel(chatCompletionRequest.getModel());
        BeanUtil.copyProperties(adapter, backend);
        params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
        IMapper mapper = getMapper(backend, adapter);
        mapper.setParameters(params);
        mapper.setPriority(backend.getPriority());
        contain.registerMapper(mapper);
    }

    public ChatCompletionResult completions(ILlmAdapter adapter, ChatCompletionRequest chatCompletionRequest) {
        if(adapter != null) {
            return adapter.completions(chatCompletionRequest);
        }
        return null;
    }

    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        return completions(chatCompletionRequest, null);
    }


    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        return streamCompletions(chatCompletionRequest, null);
    }

    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList) {
        RRException r = new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL,"{\"error\":\"Stream backend is not enabled.\"}");
        if (chatCompletionRequest.getModel() != null) {
            ILlmAdapter adapter = LlmManager.getInstance().getAdapter(chatCompletionRequest.getModel());
            if(adapter != null && notFreezingAdapter(adapter)) {
                try {
                    Observable<ChatCompletionResult> result = adapter.streamCompletions(chatCompletionRequest);
                    unfreezeAdapter(adapter);
                    return  result;
                } catch (RRException e) {
                    freezingAdapterByErrorCode(adapter, e.getCode());
                    r = e;
                }
            }
        }

        chatCompletionRequest.setModel(null);
        List<ILlmAdapter> adapters = getLlmAdapters(indexSearchDataList);
        String handle = getPolicy().getHandle();
        if(!PolicyConstants.POLLING.equals(handle)){
            for (ILlmAdapter adapter : adapters) {
                ChatCompletionRequest copy = new ChatCompletionRequest();
                BeanUtil.copyProperties(chatCompletionRequest, copy);
                if (adapter != null) {
                    try {
                        Observable<ChatCompletionResult> result = adapter.streamCompletions(copy);
                        unfreezeAdapter(adapter);
                        return result;
                    } catch (RRException e) {
                        freezingAdapterByErrorCode(adapter, e.getCode());
                        r = e;
                    }
                }
            }
        } else {
            return pollingGetStreamChatCompletionResult(chatCompletionRequest, adapters);
        }

        throw r;
    }

    private Observable<ChatCompletionResult> pollingGetStreamChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        RRException r = new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL,"{\"error\":\"failover -> backend is not enabled.\"}");

        if(chatCompletionRequest instanceof EnhanceChatCompletionRequest) {
            EnhanceChatCompletionRequest enhanceChatCompletionRequest =  (EnhanceChatCompletionRequest) chatCompletionRequest;
            int hash = enhanceChatCompletionRequest.getIp().hashCode();
            while (!ragAdapters.isEmpty()) {
                int index = Math.abs(hash) % ragAdapters.size();
                ILlmAdapter adapter = ragAdapters.get(index);
                if(adapter != null && notFreezingAdapter(adapter)) {
                    ChatCompletionRequest copy = new ChatCompletionRequest();
                    BeanUtil.copyProperties(chatCompletionRequest, copy);
                    try {
                        Observable<ChatCompletionResult> chatCompletionResultObservable = adapter.streamCompletions(copy);
                        unfreezeAdapter(adapter);
                        return chatCompletionResultObservable;
                    }
                    catch (RRException e) {
                        freezingAdapterByErrorCode(adapter, e.getCode());
                        r = e;
                    }
                }
                ragAdapters.remove(index);
            }
        } else {
            List<String> models = ragAdapters.stream().map(adapter -> {
                ModelService modelService = (ModelService) adapter;
                return modelService.getModel();
            }).collect(Collectors.toList());
            while (!models.isEmpty()) {
                String model = PollingScheduler.schedule(models);
                ILlmAdapter appointAdapter = LlmManager.getInstance().getAdapter(model);
                if(appointAdapter != null && notFreezingAdapter(appointAdapter)) {
                    ChatCompletionRequest copy = new ChatCompletionRequest();
                    BeanUtil.copyProperties(chatCompletionRequest, copy);
                    try {
                        Observable<ChatCompletionResult> result = appointAdapter.streamCompletions(copy);
                        unfreezeAdapter(appointAdapter);
                        return result;
                    } catch (RRException e) {
                        freezingAdapterByErrorCode(appointAdapter, e.getCode());
                        r = e;
                    }
                }
                models.remove(model);
            }
        }

        throw  r;
    }

    private List<ILlmAdapter> getLlmAdapters(List<IndexSearchData> indexSearchDataList) {
        // no effect backend
        List<ILlmAdapter> adapters;
        if(indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            adapters = LlmRouterDispatcher.getRagAdapter(indexSearchDataList.get(0).getText());
        } else {
            adapters = LlmManager.getInstance().getAdapters();
        }
        List<ILlmAdapter> notFreezingAdapters =
                adapters.stream().filter(adapter -> adapter != null && notFreezingAdapter(adapter)).collect(Collectors.toList());

        if(!notFreezingAdapters.isEmpty()) {
            adapters = notFreezingAdapters;
        }
        if(adapters.isEmpty()) {
            throw new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL, "{\"error\" : \"no available model\"}");
        }
        return adapters;
    }


    public Observable<ChatCompletionResult> streamCompletions(ILlmAdapter adapter, ChatCompletionRequest chatCompletionRequest) {
        if(adapter != null) {
            return adapter.streamCompletions(chatCompletionRequest);
        }
        throw new RuntimeException("Stream backend is not enabled.");
    }

    public boolean notFreezingAdapter(ILlmAdapter adapter) {
        ModelService modelService = (ModelService) adapter;
        Integer freezingCount = CacheManager.getInstance().getCount(modelService.getModel());
        if(freezingCount >= getPolicy().getMaxGen()) {
            return false;
        }
        return CacheManager.getInstance().get(modelService.getModel());
    }

    public static void freezingAdapterByErrorCode(ILlmAdapter adapter, int errorCode) {
        if(errorCode == LLMErrorConstants.PERMISSION_DENIED_ERROR
                || errorCode == LLMErrorConstants.RESOURCE_NOT_FOUND_ERROR
                || errorCode == LLMErrorConstants.INVALID_AUTHENTICATION_ERROR) {
            freezingAdapter(adapter);
        }
    }

    public static void freezingAdapter(ILlmAdapter adapter) {
        ModelService modelService = (ModelService) adapter;
        String model = modelService.getModel();
        if(CacheManager.getInstance().get(model)) {
            CacheManager.getInstance().put(modelService.getModel(), false);
            log.error("The  model {} has been blocked.",modelService.getModel());
        }
    }

    public static void unfreezeAdapter(ILlmAdapter adapter) {
        ModelService modelService = (ModelService) adapter;
        String model = modelService.getModel();
        CacheManager.getInstance().removeCount(model);
        CacheManager.getInstance().remove(model);
    }


    private IMapper getMapper(Backend backendConfig, ILlmAdapter adapter) {
        return new UniversalMapper(adapter);
    }

    public void addVectorDBContext(ChatCompletionRequest request, String context) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        String prompt = "以下是背景信息：\n--------------------\n%s\n--------------------\n" +
                "根据上下文信息而非先前知识，回答以下这个问题，回答只基于上下文信息，不要随意扩展和发散内容，不要出现上下文里没有的信息: %s";
        prompt = String.format(prompt, context, lastMessage);
        ChatCompletionUtil.setLastMessage(request, prompt);
    }

    public GetRagContext getRagContext(List<IndexSearchData> indexSearchDataList) {
        if (indexSearchDataList.isEmpty()) {
            return null;
        }
        List<String> filePaths = new ArrayList<>();
        List<String> filenames = new ArrayList<>();
        String context = indexSearchDataList.get(0).getText();
        if(indexSearchDataList.get(0).getFilepath() != null && indexSearchDataList.get(0).getFilename() != null) {
            filePaths.addAll(indexSearchDataList.get(0).getFilepath());
            filenames.addAll(indexSearchDataList.get(0).getFilename());
        }
        double firstDistance = indexSearchDataList.get(0).getDistance();
        double lastDistance = firstDistance;
        List<Double> diffList = new ArrayList<>();
        for (int i = 1;i < indexSearchDataList.size();i ++) {
            if (i == 1) {
                IndexSearchData data = indexSearchDataList.get(i);
                double diff = data.getDistance() - firstDistance;
                double threshold = diff / firstDistance;
                if (threshold < 0.25) {
                    if(data.getFilepath() != null && data.getFilename() != null) {
                        filePaths.addAll(data.getFilepath());
                        filenames.addAll(data.getFilename());
                    }
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
                    if(data.getFilepath() != null && data.getFilename() != null) {
                        filePaths.addAll(data.getFilepath());
                        filenames.addAll(data.getFilename());
                    }
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
                    if(data.getFilepath() != null && data.getFilename() != null) {
                        filePaths.addAll(data.getFilepath());
                        filenames.addAll(data.getFilename());
                    }
                } else {
                    break;
                }
            }
        }
        return GetRagContext.builder()
                .filenames(filenames)
                .filePaths(filePaths)
                .context(context)
                .build();
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
