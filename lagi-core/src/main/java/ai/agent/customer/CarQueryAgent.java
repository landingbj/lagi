package ai.agent.customer;

import ai.agent.customer.tools.CarQueryTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class CarQueryAgent extends CustomerAgent {

    public CarQueryAgent(AgentConfig agentConfig) {
        super(agentConfig);
        CarQueryTool carQueryTool = new CarQueryTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(carQueryTool.getToolInfo(), finishTool.getToolInfo());
    }
}
