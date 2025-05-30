package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.HistoricalFigureInfoTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

@Deprecated
public class HistoricalFigureInfoAgent extends CustomerAgent {
    public HistoricalFigureInfoAgent(AgentConfig agentConfig) {
        super(agentConfig);
        HistoricalFigureInfoTool historicalFigureInfoTool = new HistoricalFigureInfoTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(historicalFigureInfoTool.getToolInfo(), finishTool.getToolInfo());
    }
}
