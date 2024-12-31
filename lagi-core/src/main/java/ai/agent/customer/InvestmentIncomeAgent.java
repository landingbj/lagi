package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.InvestmentIncomeTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class InvestmentIncomeAgent extends CustomerAgent {

    public InvestmentIncomeAgent(AgentConfig agentConfig) {
        super(agentConfig);
        InvestmentIncomeTool investmentIncomeTool = new InvestmentIncomeTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(investmentIncomeTool.getToolInfo(), finishTool.getToolInfo());
    }
}
