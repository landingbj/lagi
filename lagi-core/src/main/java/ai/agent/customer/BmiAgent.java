package ai.agent.customer;

import ai.agent.customer.tools.BMITool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class BmiAgent extends CustomerAgent {
    public BmiAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        BMITool bmiTool = new BMITool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(bmiTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
