/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

package ai.llm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ai.llm.LLMManager;
import ai.llm.adapter.ILlmAdapter;
import ai.common.pojo.Backend;
import ai.common.pojo.Configuration;
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

    private List<Backend> chatBackends;


    public CompletionsService(Configuration config) {
        this.config = config;
        this.chatBackends = this.config.getLLM().getChatBackends().stream().sorted((b1, b2) ->{
            if(Objects.equals(b1.getPriority(), b2.getPriority())) {
                return 0;
            }
            if(b1.getPriority() - b2.getPriority() > 0) {
                return -1;
            }
            return 1;
        }).collect(Collectors.toList());

        if(chatBackends.isEmpty()) {
            throw new RuntimeException("No stream backend matched");
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
                        IMapper mapper = getMapper(backend);
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
                    IMapper mapper = getMapper(backend);
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
        for(Backend backend : this.chatBackends) {
            ILlmAdapter adapter = getAdapter(backend);
            try {
                return adapter.streamCompletions(chatCompletionRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Stream backend is not enabled.");
    }

    private ILlmAdapter getAdapter(Backend backendConfig) {
        return LLMManager.getAdapter(backendConfig.getName());
    }

    private IMapper getMapper(Backend backendConfig) {
        if(backendConfig.getType().equalsIgnoreCase(LagiGlobal.LLM_TYPE_LANDING)) {
            return  new LandingMapper();
        }
        return new UniversalMapper(getAdapter(backendConfig));
    }
}
