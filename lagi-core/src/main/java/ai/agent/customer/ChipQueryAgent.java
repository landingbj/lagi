package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.ChipQueryTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class ChipQueryAgent extends CustomerAgent {

    public ChipQueryAgent(AgentConfig agentConfig) {
        super(agentConfig);
        ChipQueryTool chipQueryTool = new ChipQueryTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(chipQueryTool.getToolInfo(), finishTool.getToolInfo());
    }
}
