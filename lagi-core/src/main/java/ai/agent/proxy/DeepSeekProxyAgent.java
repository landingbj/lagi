package ai.agent.proxy;

import ai.config.pojo.AgentConfig;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.adapter.impl.ProxyLlmAdapter;
import ai.llm.service.CompletionsService;
import ai.manager.AIManager;
import ai.manager.LlmManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

public class DeepSeekProxyAgent extends ProxyAgent{


    protected CompletionsService completionsService;

    @Slf4j
    static class DeepSeekLlmManager extends AIManager<ILlmAdapter> {

        private static final DeepSeekLlmManager INSTANCE = new DeepSeekLlmManager();

        private DeepSeekLlmManager() {

        }

        public static DeepSeekLlmManager getInstance(){
            return INSTANCE;
        }

        @Override
        public void register(String key, ILlmAdapter adapter) {
            ProxyLlmAdapter proxyLlmAdapter = new ProxyLlmAdapter(adapter);
            ILlmAdapter tempAdapter = aiMap.putIfAbsent(key, proxyLlmAdapter);
            if (tempAdapter != null) {
                log.error("Adapter {} name {} is already exists!!", proxyLlmAdapter.getClass().getName(), key);
            }
        }
    }


    public DeepSeekProxyAgent(AgentConfig agentConfig)
    {
        this.agentConfig = agentConfig;
        DeepSeekLlmManager instance = DeepSeekLlmManager.getInstance();
        instance.register("deepseek-chat", LlmManager.getInstance().getAdapter("deepseek-chat"));
        this.completionsService = new CompletionsService(instance);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        data.setModel("deepseek-chat");
        return completionsService.completions(data);
    }

    @Override
    public Observable<ChatCompletionResult> stream(ChatCompletionRequest data) {
        data.setModel("deepseek-chat");
        return completionsService.streamCompletions(data);
    }

    @Override
    public boolean canStream() {
        return true;
    }
}
