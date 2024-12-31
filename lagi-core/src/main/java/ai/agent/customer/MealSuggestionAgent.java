package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.MealSuggestionTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class MealSuggestionAgent extends CustomerAgent {
    public MealSuggestionAgent(AgentConfig agentConfig) {
        super(agentConfig);
        MealSuggestionTool mealSuggestionTool = new MealSuggestionTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(mealSuggestionTool.getToolInfo(), finishTool.getToolInfo());
    }
}
