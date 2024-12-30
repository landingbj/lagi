package ai.agent.customer;

import ai.agent.customer.tools.ConstellationLuckTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class ConstellationLuckAgent  extends CustomerAgent {
    public ConstellationLuckAgent(AgentConfig agentConfig) {
        super(agentConfig);
        ConstellationLuckTool weatherSearchTool = new ConstellationLuckTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
