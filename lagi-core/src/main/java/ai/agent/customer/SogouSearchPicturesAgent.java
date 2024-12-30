package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.SogouSearchPicturesTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class SogouSearchPicturesAgent extends CustomerAgent {
    public SogouSearchPicturesAgent(AgentConfig agentConfig) {
        super(agentConfig);
        SogouSearchPicturesTool weatherSearchTool = new SogouSearchPicturesTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
