package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.DailyRumorRefutationTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class DailyRumorRefutationAgent extends CustomerAgent {
    public DailyRumorRefutationAgent(AgentConfig agentConfig) {
        super(agentConfig);
        DailyRumorRefutationTool dailyRumorRefutationTool = new DailyRumorRefutationTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(dailyRumorRefutationTool.getToolInfo(), finishTool.getToolInfo());
    }
}
