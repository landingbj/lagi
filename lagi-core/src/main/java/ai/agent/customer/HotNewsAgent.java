package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.HotNewsTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class HotNewsAgent extends CustomerAgent {
    public HotNewsAgent(AgentConfig agentConfig) {
        super(agentConfig);
        HotNewsTool hotNewsTool = new HotNewsTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(hotNewsTool.getToolInfo(), finishTool.getToolInfo());
    }
}