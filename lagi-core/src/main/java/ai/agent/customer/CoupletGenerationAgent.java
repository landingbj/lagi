package ai.agent.customer;

import ai.agent.customer.tools.CoupletGenerationTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

@Deprecated
public class CoupletGenerationAgent extends CustomerAgent {
    public CoupletGenerationAgent(AgentConfig agentConfig) {
        super(agentConfig);
        CoupletGenerationTool coupletGenerationTool = new CoupletGenerationTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(coupletGenerationTool.getToolInfo(), finishTool.getToolInfo());
    }
}
