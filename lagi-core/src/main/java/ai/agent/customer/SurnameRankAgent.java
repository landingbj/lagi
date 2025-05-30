package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.SurnameRankTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

@Deprecated
public class SurnameRankAgent extends CustomerAgent {

    public SurnameRankAgent(AgentConfig agentConfig) {
        super(agentConfig);
        SurnameRankTool surnameRankTool = new SurnameRankTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(surnameRankTool.getToolInfo(), finishTool.getToolInfo());
    }
}
