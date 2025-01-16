package ai.agents.example;

import ai.agent.customer.CustomerAgent;
import ai.agent.customer.tools.FinishTool;
import ai.agents.example.tools.IPAddressLookupTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class IPAddressLookupAgent extends CustomerAgent {
    public IPAddressLookupAgent(AgentConfig agentConfig) {
        super(agentConfig);
        IPAddressLookupTool weatherSearchTool = new IPAddressLookupTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
