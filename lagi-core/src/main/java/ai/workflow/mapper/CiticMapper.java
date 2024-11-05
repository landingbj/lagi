package ai.workflow.mapper;

import ai.common.pojo.Configuration;
import ai.config.pojo.AgentConfig;
import ai.learn.questionAnswer.KShingle;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;

import java.util.HashMap;
import java.util.Map;


public class CiticMapper extends BaseMapper {
    protected static final Map<String, AgentConfig> AGENT_CONFIG_MAP = new HashMap<>();

    static {
        Configuration config = LagiGlobal.getConfig();
        for (AgentConfig agentConfig : config.getAgents()) {
            AGENT_CONFIG_MAP.put(agentConfig.getDriver(), agentConfig);
        }
    }

    public double getSimilarity(ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) {
        KShingle kShingle = new KShingle();
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        double[] similarity = kShingle.similarity(question, answer, 2);
        return similarity[0];
    }
}
