package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.MeaningSearchTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class MeaningSearchAgent extends CustomerAgent {
    public MeaningSearchAgent(AgentConfig agentConfig) {
        super(agentConfig);
        MeaningSearchTool meaningSearchTool = new MeaningSearchTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(meaningSearchTool.getToolInfo(), finishTool.getToolInfo());
    }
}
