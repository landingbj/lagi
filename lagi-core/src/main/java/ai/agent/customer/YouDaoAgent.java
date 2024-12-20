package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.YouDaoTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class YouDaoAgent extends CustomerAgent {

    public YouDaoAgent(AgentConfig agentConfig) {
        super(agentConfig);
        YouDaoTool youDaoTool = new YouDaoTool(agentConfig.getAppId(), agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(youDaoTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
