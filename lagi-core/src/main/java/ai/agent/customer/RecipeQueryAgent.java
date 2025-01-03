package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.RecipeQueryTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class RecipeQueryAgent extends CustomerAgent {
    public RecipeQueryAgent(AgentConfig agentConfig) {
        super(agentConfig);
        RecipeQueryTool recipeQueryTool = new RecipeQueryTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(recipeQueryTool.getToolInfo(), finishTool.getToolInfo());
    }
}
