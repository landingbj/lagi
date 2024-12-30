package ai.agent.customer;

import ai.agent.customer.tools.AnimePicturesTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class AnimePicturesAgent extends CustomerAgent {
    public AnimePicturesAgent(AgentConfig agentConfig) {
        super(agentConfig);
        AnimePicturesTool weatherSearchTool = new AnimePicturesTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
