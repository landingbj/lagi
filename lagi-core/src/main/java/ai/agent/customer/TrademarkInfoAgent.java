package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.TrademarkInfoTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class TrademarkInfoAgent extends CustomerAgent {
    public TrademarkInfoAgent(AgentConfig agentConfig) {
        super(agentConfig);
        TrademarkInfoTool trademarkInfoTool = new TrademarkInfoTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(trademarkInfoTool.getToolInfo(), finishTool.getToolInfo());
    }
}
