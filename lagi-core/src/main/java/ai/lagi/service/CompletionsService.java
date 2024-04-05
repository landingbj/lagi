/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

package ai.lagi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.lagi.adapter.ILlmAdapter;
import ai.lagi.adapter.impl.ErnieAdapter;
import ai.lagi.adapter.impl.GPTAdapter;
import ai.lagi.adapter.impl.QwenAdapter;
import ai.lagi.adapter.impl.VicunaAdapter;
import ai.migrate.pojo.Backend;
import ai.migrate.pojo.Configuration;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.FastDirectContainer;
import ai.mr.mapper.llm.*;
import ai.mr.reducer.llm.QaReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
import ai.utils.SensitiveWordUtil;
import io.reactivex.Observable;
import weixin.tools.TulingThread;

public class CompletionsService {
    private static TulingThread tulingProcessor = null;

    private Configuration config;

    static {
        if (tulingProcessor == null) {
            tulingProcessor = new TulingThread();
            tulingProcessor.setDaemon(true);
            tulingProcessor.start();
        }
    }

    private Backend streamBackendConfig;

    public CompletionsService(Configuration config) {
        this.config = config;
        String streamBackend = this.config.getLLM().getStream_backend();
        for (Backend backend : this.config.getLLM().getBackends()) {
            if (backend.getName().equals(streamBackend)) {
                streamBackendConfig = backend;
            }
        }
        if (streamBackendConfig == null) {
            throw new RuntimeException("No stream_backend parameter was specified.");
        }
    }

    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        ChatCompletionResult answer = null;

        try (IRContainer contain = new FastDirectContainer()) {
            if (chatCompletionRequest.getModel() != null) {
                for (Backend backend : this.config.getLLM().getBackends()) {
                    if (!backend.getEnable()) {
                        continue;
                    }
                    if (chatCompletionRequest.getModel().equals(backend.getModel())) {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
                        params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
                        IMapper mapper = getMapper(backend.getType());
                        mapper.setParameters(params);
                        mapper.setPriority(backend.getPriority());
                        contain.registerMapper(mapper);
                    }
                }
            } else {
                for (Backend backend : this.config.getLLM().getBackends()) {
                    if (!backend.getEnable()) {
                        continue;
                    }
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put(LagiGlobal.CHAT_COMPLETION_REQUEST, chatCompletionRequest);
                    params.put(LagiGlobal.CHAT_COMPLETION_CONFIG, backend);
                    IMapper mapper = getMapper(backend.getType());
                    mapper.setParameters(params);
                    mapper.setPriority(backend.getPriority());
                    contain.registerMapper(mapper);
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
        ILlmAdapter adapter = getAdapter(this.streamBackendConfig);
        if (!this.streamBackendConfig.getEnable()) {
            throw new RuntimeException("Stream backend is not enabled.");
        }
        return adapter.streamCompletions(chatCompletionRequest);
    }

    private ILlmAdapter getAdapter(Backend backendConfig) {
        String type = backendConfig.getType();
        ILlmAdapter adapter = null;
        if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_VICUNA)) {
            adapter = new VicunaAdapter(backendConfig);
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_GPT)) {
            adapter = new GPTAdapter(backendConfig);
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_Qwen)) {
            adapter = new QwenAdapter(backendConfig);
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_ERNIE)) {
            adapter = new ErnieAdapter(backendConfig);
        }
        return adapter;
    }

    private IMapper getMapper(String type) {
        IMapper mapper = null;
        if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_QA)) {
            mapper = new QaPairMapper();
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_TREE)) {
            mapper = new SimulatingTreeMapper();
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_TURING)) {
            mapper = new TulingMapper();
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_VICUNA)) {
            mapper = new VicunaMapper();
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_GPT)) {
            mapper = new GPTMapper();
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_Qwen)) {
            mapper = new QwenMapper();
        } else if (type.equalsIgnoreCase(LagiGlobal.LLM_TYPE_ERNIE)) {
            mapper = new ErnieMapper();
        }
        return mapper;
    }
}
