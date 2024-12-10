package ai.agent.customer;

import ai.agent.customer.tools.DishonestPersonSearchTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class DishonestPersonSearchAgent extends CustomerAgent {
    public DishonestPersonSearchAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        DishonestPersonSearchTool dishonestPersonSearchTool = new DishonestPersonSearchTool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(dishonestPersonSearchTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
