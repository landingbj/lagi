package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.HistoryInTodayTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class HistoryInToDayAgent extends CustomerAgent {
    public HistoryInToDayAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        HistoryInTodayTool tool = new HistoryInTodayTool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(tool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
