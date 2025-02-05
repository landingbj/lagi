
package ai.agent.customer;

import ai.agent.customer.tools.CityTravelRouteTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class CityTravelRouteAgent extends CustomerAgent {

    public CityTravelRouteAgent(AgentConfig agentConfig) {
        super(agentConfig);
        CityTravelRouteTool cityTravelRouteTool = new CityTravelRouteTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(cityTravelRouteTool.getToolInfo(), finishTool.getToolInfo());
    }
}
