package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.RandomAnimePicturesTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class RandomAnimePicturesAgent extends CustomerAgent {
    public RandomAnimePicturesAgent(AgentConfig agentConfig) {
        super(agentConfig);
        RandomAnimePicturesTool weatherSearchTool = new RandomAnimePicturesTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
