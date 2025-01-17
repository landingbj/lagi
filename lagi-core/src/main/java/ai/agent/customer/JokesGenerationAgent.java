package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.JokesGenerationTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class JokesGenerationAgent extends CustomerAgent {
    public JokesGenerationAgent(AgentConfig agentConfig) {
        super(agentConfig);
        JokesGenerationTool searchTool = new JokesGenerationTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(searchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
