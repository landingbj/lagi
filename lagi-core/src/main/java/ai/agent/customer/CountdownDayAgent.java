package ai.agent.customer;

import ai.agent.customer.tools.CountdownDayTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class CountdownDayAgent extends CustomerAgent {

    public CountdownDayAgent(AgentConfig agentConfig) {
        super(agentConfig);
        CountdownDayTool countdownDayTool = new CountdownDayTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(countdownDayTool.getToolInfo(), finishTool.getToolInfo());
    }
}
