package ai.agent.chat.rag;

import ai.agent.chat.ChatAgent;
import ai.config.ContextLoader;
import ai.config.pojo.AgentConfig;
import ai.config.pojo.RAGFunction;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.OkHttpUtil;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RagAgent extends ChatAgent {

    private final Gson gson = new Gson();

    private final RAGFunction RAG_CONFIG = ContextLoader.configuration.getStores().getRag();

    private final AgentConfig agentConfig;

    @Getter
    private final String agentName;

    public RagAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.agentName = agentConfig.getName();
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {

        String responseJson = null;
        try {
            responseJson = OkHttpUtil.post(agentConfig.getEndpoint() + "/v1/chat/completions", gson.toJson(data));
        } catch (IOException e) {
            String SAMPLE_COMPLETION_RESULT_PATTERN = "{\"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"%s\"}}]}";
            responseJson = String.format(SAMPLE_COMPLETION_RESULT_PATTERN, RAG_CONFIG.getDefaultText());
            log.error("RagMapper.myMapping: OkHttpUtil.post error", e);
        }
        if(responseJson != null) {
            return  gson.fromJson(responseJson, ChatCompletionResult.class);
        }
        return null;
    }
}
