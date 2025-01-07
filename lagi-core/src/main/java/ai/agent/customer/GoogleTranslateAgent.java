package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.GoogleTranslateTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class GoogleTranslateAgent extends CustomerAgent {
    public GoogleTranslateAgent(AgentConfig agentConfig) {
        super(agentConfig);
        GoogleTranslateTool googleTranslateTool = new GoogleTranslateTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(googleTranslateTool.getToolInfo(), finishTool.getToolInfo());
    }
}
