package ai.agent.customer;

import ai.agent.customer.tools.LotteryResultsTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class LotteryResultsAgent extends CustomerAgent {

    public LotteryResultsAgent(AgentConfig agentConfig) {
        super(agentConfig);

        LotteryResultsTool lotteryResultsTool = new LotteryResultsTool();
        FinishTool finishTool = new FinishTool();

        this.toolInfoList = Lists.newArrayList(lotteryResultsTool.getToolInfo(), finishTool.getToolInfo());
    }
}
