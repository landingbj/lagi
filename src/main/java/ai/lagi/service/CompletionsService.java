/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * KnowledgeService.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

/**
 * 
 */
package ai.lagi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.migrate.pojo.Backend;
import ai.migrate.pojo.Configuration;
import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.FastDirectContainer;
import ai.mr.mapper.llm.GPTMapper;
import ai.mr.mapper.llm.QwenMapper;
import ai.mr.mapper.llm.VicunaMapper;
import ai.mr.mapper.llm.QaPairMapper;
import ai.mr.mapper.llm.SimulatingTreeMapper;
import ai.mr.mapper.llm.TulingMapper;
import ai.mr.reducer.llm.QaReducer;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
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

    public CompletionsService(Configuration config) {
        this.config = config;
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
            }
        }

        return answer;
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
        }
        return mapper;
    }
}
