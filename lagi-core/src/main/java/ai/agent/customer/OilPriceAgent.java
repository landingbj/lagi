package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.OilPriceSearchTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class OilPriceAgent extends CustomerAgent {
    public OilPriceAgent(AgentConfig agentConfig) {
        super(agentConfig);
        OilPriceSearchTool oilPriceSearchTool = new OilPriceSearchTool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(oilPriceSearchTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
