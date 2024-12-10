package ai.agent.customer;

import ai.agent.customer.tools.CalendarTool;
import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.HighSpeedTicketTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class HighSpeedTicketAgent extends CustomerAgent {
    public HighSpeedTicketAgent(AgentConfig agentConfig) {
        this.agentName = agentConfig.getName();
        HighSpeedTicketTool tool = new HighSpeedTicketTool(agentConfig.getToken());
        CalendarTool calendarTool = new CalendarTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(tool.getToolInfo(),
                calendarTool.getToolInfo(),
                finishTool.getToolInfo());

    }
}
