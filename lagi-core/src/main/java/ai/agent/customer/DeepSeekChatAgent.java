package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.DeepSeekChatTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class DeepSeekChatAgent extends CustomerAgent {
    public DeepSeekChatAgent(AgentConfig agentConfig) {
        super(agentConfig);
        DeepSeekChatTool deepSeekChatTool = new DeepSeekChatTool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(deepSeekChatTool.getToolInfo(), finishTool.getToolInfo());
    }
}
