package ai.agent.customer;

import ai.agent.customer.tools.DrivingLicenseSearchTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class DrivingLicenseSearchAgent extends CustomerAgent {

    public DrivingLicenseSearchAgent(AgentConfig agentConfig) {
        super(agentConfig);
        DrivingLicenseSearchTool drivingLicenseSearchTool = new DrivingLicenseSearchTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(drivingLicenseSearchTool.getToolInfo(), finishTool.getToolInfo());
    }
}
