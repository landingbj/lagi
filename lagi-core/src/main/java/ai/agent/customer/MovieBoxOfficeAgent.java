package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.MovieBoxOfficeTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class MovieBoxOfficeAgent extends CustomerAgent {
    public MovieBoxOfficeAgent(AgentConfig agentConfig) {
        super(agentConfig);
        MovieBoxOfficeTool movieBoxOfficeTool = new MovieBoxOfficeTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(movieBoxOfficeTool.getToolInfo(), finishTool.getToolInfo());
    }
}
