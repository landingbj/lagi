package ai.agents.example;

import ai.agent.customer.CustomerAgent;
import ai.agents.example.tools.BMITool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class BmiAgent extends CustomerAgent {
    public BmiAgent(AgentConfig agentConfig) {
        super(agentConfig);
        BMITool bmiTool = new BMITool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(bmiTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
