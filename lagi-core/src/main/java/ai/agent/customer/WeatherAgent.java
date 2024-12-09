package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.WeatherSearchTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class WeatherAgent extends CustomerAgent {
    public WeatherAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        WeatherSearchTool weatherSearchTool = new WeatherSearchTool(agentConfig.getToken());
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(weatherSearchTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
