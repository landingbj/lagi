package ai.agent.social;

import ai.agent.AgentGlobal;
import ai.agent.pojo.AgentParam;
import ai.agent.pojo.SocialAgentParam;


public class QQAgent extends SocialAgent {
    private static final String APP_TYPE = AgentGlobal.APP_TYPE_QQ;

    public QQAgent(AgentParam param) {
        super(APP_TYPE, (SocialAgentParam) param);
    }
}
