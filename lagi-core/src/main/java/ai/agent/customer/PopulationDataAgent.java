package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.PopulationDataTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class PopulationDataAgent extends CustomerAgent {
    public PopulationDataAgent(AgentConfig agentConfig) {
        super(agentConfig);
        PopulationDataTool populationDataTool = new PopulationDataTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(populationDataTool.getToolInfo(), finishTool.getToolInfo());
    }
}
