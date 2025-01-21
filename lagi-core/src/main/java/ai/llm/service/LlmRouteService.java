/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

package ai.llm.service;

import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.pojo.Backend;
import ai.config.ContextLoader;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.utils.LLMErrorConstants;
import ai.llm.utils.PollingScheduler;
import ai.manager.AIManager;
import ai.manager.LlmManager;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.FastDirectContainer;
import ai.mr.mapper.llm.UniversalMapper;
import ai.mr.reducer.llm.QaReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
import ai.utils.SensitiveWordUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LlmRouteService {
    private final AIManager<ILlmAdapter> llmAdapterAIManager;

    public LlmRouteService() {
        this.llmAdapterAIManager = LlmManager.getInstance();
    }

    public static String getRoute() {
        return ContextLoader.configuration.getFunctions().getChat().getRoute();
    }


    public ChatCompletionResult failoverGetChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        chatCompletionRequest.setModel(null);
        RRException r = new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL, "{\"error\":\"failover -> backend is not enabled.\"}");
        for (ILlmAdapter adapter : ragAdapters) {
            ChatCompletionRequest copy = new ChatCompletionRequest();
            BeanUtil.copyProperties(chatCompletionRequest, copy);
            try {
                ChatCompletionResult result = SensitiveWordUtil.filter(adapter.completions(copy));
                FreezingService.unfreezeAdapter(adapter);
                return result;
            } catch (RRException e) {
                FreezingService.freezingAdapterByErrorCode(adapter, e.getCode());
                r = e;
            }
        }
        throw r;
    }


    public ChatCompletionResult parallelGetChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        chatCompletionRequest.setModel(null);
        ChatCompletionResult answer;
        try (FastDirectContainer contain = new FastDirectContainer() {
            @Override
            public void onMapperFail(String mapperName, Integer priority, Throwable throwable) {
                super.onMapperFail(mapperName, priority, throwable);
                IMapper iMapper = mappersGroup.get(mapperName);
                RRException exception;
                if (!(throwable instanceof RRException)) {
                    return;
                }
                exception = (RRException) throwable;
                if (!(iMapper instanceof UniversalMapper)) {
                    return;
                }
                UniversalMapper universalMapper = (UniversalMapper) iMapper;
                ILlmAdapter adapter = universalMapper.getAdapter();
                FreezingService.freezingAdapterByErrorCode(adapter, exception.getCode());
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

    public ChatCompletionResult pollingGetChatCompletionResult(ChatCompletionRequest chatCompletionRequest, List<ILlmAdapter> ragAdapters) {
        chatCompletionRequest.setModel(null);
        RRException r = new RRException(LLMErrorConstants.NO_AVAILABLE_MODEL, "{\"error\":\"failover -> backend is not enabled.\"}");
        // no ipaddress use sample polling
        if (chatCompletionRequest instanceof EnhanceChatCompletionRequest) {
            EnhanceChatCompletionRequest enhanceChatCompletionRequest = (EnhanceChatCompletionRequest) chatCompletionRequest;
            int hash = enhanceChatCompletionRequest.getIp().hashCode();
            while (!ragAdapters.isEmpty()) {
                int index = Math.abs(hash) % ragAdapters.size();
                ILlmAdapter adapter = ragAdapters.get(index);
                if (adapter != null && FreezingService.notFreezingAdapter(adapter)) {
                    ChatCompletionRequest copy = new ChatCompletionRequest();
                    BeanUtil.copyProperties(chatCompletionRequest, copy);
                    try {
                        ChatCompletionResult result = SensitiveWordUtil.filter(adapter.completions(copy));
                        FreezingService.unfreezeAdapter(adapter);
                        return result;
                    } catch (RRException e) {
                        FreezingService.freezingAdapterByErrorCode(adapter, e.getCode());
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
                ILlmAdapter appointAdapter = llmAdapterAIManager.getAdapter(model);
                if (appointAdapter != null && FreezingService.notFreezingAdapter(appointAdapter)) {
                    ChatCompletionRequest copy = new ChatCompletionRequest();
                    BeanUtil.copyProperties(chatCompletionRequest, copy);
                    try {
                        ChatCompletionResult result = SensitiveWordUtil.filter(appointAdapter.completions(copy));
                        FreezingService.unfreezeAdapter(appointAdapter);
                        return result;
                    } catch (RRException e) {
                        FreezingService.freezingAdapterByErrorCode(appointAdapter, e.getCode());
                        r = e;
                    }
                }
                models.remove(model);
            }
        }
        throw r;
    }

    private void registerMapper(ChatCompletionRequest chatCompletionRequest, ILlmAdapter adapter, IRContainer contain) {
        Map<String, Object> params = new HashMap<>();
        params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
        Backend backend = new Backend();
        backend.setModel(chatCompletionRequest.getModel());
        BeanUtil.copyProperties(adapter, backend);
        params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
        IMapper mapper = getMapper(adapter);
        mapper.setParameters(params);
        mapper.setPriority(backend.getPriority());
        contain.registerMapper(mapper);
    }

    private IMapper getMapper(ILlmAdapter adapter) {
        return new UniversalMapper(adapter);
    }
}
