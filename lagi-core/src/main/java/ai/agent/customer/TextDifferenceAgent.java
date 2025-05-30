package ai.agent.customer;

import ai.agent.customer.tools.TextDifferenceTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

@Deprecated
public class TextDifferenceAgent extends CustomerAgent {

    public TextDifferenceAgent(AgentConfig agentConfig) {
        super(agentConfig);

        TextDifferenceTool textDifferenceTool = new TextDifferenceTool();
        FinishTool finishTool = new FinishTool();

        this.toolInfoList = Lists.newArrayList(textDifferenceTool.getToolInfo(), finishTool.getToolInfo());
    }
}
