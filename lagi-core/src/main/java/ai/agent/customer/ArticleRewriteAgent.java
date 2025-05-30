package ai.agent.customer;

import ai.agent.customer.tools.FinishTool;
import ai.agent.customer.tools.ArticleRewriteTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

@Deprecated
public class ArticleRewriteAgent extends CustomerAgent {
    public ArticleRewriteAgent(AgentConfig agentConfig) {
        super(agentConfig);
        ArticleRewriteTool articleRewriteTool = new ArticleRewriteTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(articleRewriteTool.getToolInfo(), finishTool.getToolInfo());
    }
}
