package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.SparkDialogTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class SparkDialogAgent extends CustomerAgent {

    public SparkDialogAgent(AgentConfig agentConfig) {
        super(agentConfig);
        SparkDialogTool sparkDialogTool = new SparkDialogTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(sparkDialogTool.getToolInfo(), finishTool.getToolInfo());
    }
}
