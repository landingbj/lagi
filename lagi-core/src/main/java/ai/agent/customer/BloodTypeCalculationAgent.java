package ai.agent.customer;

import ai.agent.customer.tools.BloodTypeCalculationTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class BloodTypeCalculationAgent extends CustomerAgent {

    public BloodTypeCalculationAgent(AgentConfig agentConfig) {
        super(agentConfig);
        BloodTypeCalculationTool bloodTypeCalculationTool = new BloodTypeCalculationTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(bloodTypeCalculationTool.getToolInfo(), finishTool.getToolInfo());
    }
}
