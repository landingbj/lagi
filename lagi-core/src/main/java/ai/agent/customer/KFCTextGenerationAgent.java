package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.KFCTextGenerationTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class KFCTextGenerationAgent extends CustomerAgent{
    public KFCTextGenerationAgent(AgentConfig agentConfig) {
        super(agentConfig);
        KFCTextGenerationTool weatherSearchTool = new KFCTextGenerationTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
