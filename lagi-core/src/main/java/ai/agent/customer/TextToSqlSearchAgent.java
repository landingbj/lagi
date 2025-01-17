package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.TextToSqlSearchTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class TextToSqlSearchAgent extends CustomerAgent {
    public TextToSqlSearchAgent(AgentConfig agentConfig) {
        super(agentConfig);
        TextToSqlSearchTool searchTool = new TextToSqlSearchTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(searchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
