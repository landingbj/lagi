package ai.agent.customer;

import ai.agent.customer.tools.BaiduSearchPicturesTool;
import ai.agent.customer.tools.FinishTool;
import ai.config.pojo.AgentConfig;
import com.google.common.collect.Lists;

public class BaiduSearchPicturesAgent  extends CustomerAgent {
    public BaiduSearchPicturesAgent(AgentConfig agentConfig) {
        super(agentConfig);
        BaiduSearchPicturesTool searchTool = new BaiduSearchPicturesTool();
        FinishTool finishTool = new FinishTool();
        this.toolInfoList = Lists.newArrayList(searchTool.getToolInfo(),
                finishTool.getToolInfo());
    }
}
