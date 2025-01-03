package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.TextConversionTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class TextConversionAgent extends CustomerAgent {
    public TextConversionAgent(AgentConfig agentConfig) {
        super(agentConfig);
        TextConversionTool textConversionTool = new TextConversionTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(textConversionTool.getToolInfo(), finishTool.getToolInfo());
    }
}
