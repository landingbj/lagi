package ai.agents.example;

import ai.agent.customer.CustomerAgent;
import ai.agents.example.tools.BellePicturesTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class BellePicturesAgent  extends CustomerAgent {
    public BellePicturesAgent(AgentConfig agentConfig) {
        super(agentConfig);
        BellePicturesTool weatherSearchTool1 = new BellePicturesTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(
                weatherSearchTool1.getToolInfo(),
                finishTool.getToolInfo());
    }
}
