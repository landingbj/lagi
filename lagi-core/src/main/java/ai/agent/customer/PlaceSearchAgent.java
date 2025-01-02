package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.PlaceSearchTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class PlaceSearchAgent extends CustomerAgent {

    public PlaceSearchAgent(AgentConfig agentConfig) {
        super(agentConfig);
        PlaceSearchTool placeSearchTool = new PlaceSearchTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(placeSearchTool.getToolInfo(), finishTool.getToolInfo());
    }
}
