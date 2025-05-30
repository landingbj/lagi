package ai.agent.customer;

import ai.agent.customer.tools.TextCorrectorTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

@Deprecated
public class TextCorrectorAgent extends CustomerAgent {

    public TextCorrectorAgent(AgentConfig agentConfig) {
        super(agentConfig);

        TextCorrectorTool textCorrectorTool = new TextCorrectorTool();
        FinishTool finishTool = new FinishTool();

        this.toolInfoList = Lists.newArrayList(textCorrectorTool.getToolInfo(), finishTool.getToolInfo());
    }
}
