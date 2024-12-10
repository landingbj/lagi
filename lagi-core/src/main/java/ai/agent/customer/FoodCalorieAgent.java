package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.FoodCalorieTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class FoodCalorieAgent extends CustomerAgent {
    public FoodCalorieAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        FoodCalorieTool foodCalorieTool = new FoodCalorieTool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(foodCalorieTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
