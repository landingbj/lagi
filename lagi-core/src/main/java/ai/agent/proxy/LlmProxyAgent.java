package ai.agent.proxy;

import ai.config.pojo.AgentConfig;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.utils.RouterParser;

public class LlmProxyAgent extends ProxyAgent{

    private final String defaultModel;
    protected CompletionsService completionsService = new CompletionsService();

    public LlmProxyAgent(String defaultModel)
    {
        if(RouterParser.WILDCARD_STRING.equals(defaultModel)) {
            defaultModel = null;
        }
        this.defaultModel = defaultModel;
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setId(0);
        agentConfig.setName(defaultModel);
        agentConfig.setIsFeeRequired(false);
        agentConfig.setAppId(defaultModel);
        this.setAgentConfig(agentConfig);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        data.setModel(defaultModel);
        return completionsService.completions(data);
    }

}
