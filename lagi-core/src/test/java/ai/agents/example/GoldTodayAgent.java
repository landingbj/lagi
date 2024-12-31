package ai.agents.example;

import ai.agent.customer.CustomerAgent;
import ai.agent.customer.tools.FinishTool;
import ai.agents.example.tools.GoldTodayTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class GoldTodayAgent extends CustomerAgent {
    public GoldTodayAgent(AgentConfig agentConfig) {
        super(agentConfig);
        GoldTodayTool searchTool = new GoldTodayTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(searchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
