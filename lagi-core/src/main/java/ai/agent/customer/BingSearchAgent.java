package ai.agent.customer;

import ai.agent.customer.tools.BingSearchTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class BingSearchAgent extends CustomerAgent {

    public BingSearchAgent(AgentConfig agentConfig) {
        super(agentConfig);

        BingSearchTool bingSearchTool = new BingSearchTool();
        FinishTool finishTool = new FinishTool();

        this.toolInfoList = Lists.newArrayList(bingSearchTool.getToolInfo(), finishTool.getToolInfo());
    }
}
