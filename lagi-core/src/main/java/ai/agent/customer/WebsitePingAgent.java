package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.WebsitePingTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class WebsitePingAgent extends CustomerAgent {
    public WebsitePingAgent(AgentConfig agentConfig) {
        super(agentConfig);
        WebsitePingTool websitePingTool = new WebsitePingTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(websitePingTool.getToolInfo(), finishTool.getToolInfo());
    }
}
