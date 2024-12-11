package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.ImageGenTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class ImageGenAgent extends CustomerAgent {

    public ImageGenAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        ImageGenTool imageGenTool = new ImageGenTool(agentConfig.getEndpoint());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(imageGenTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
