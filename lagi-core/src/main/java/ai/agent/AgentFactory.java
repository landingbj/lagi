package ai.agent;

import ai.agent.pojo.AgentParam;
import ai.agent.social.*;
import ai.config.pojo.AgentConfig;

public class AgentFactory {
    public static Agent getAgent(AgentConfig agentConfig, AgentParam agentParam) {
        switch (agentConfig.getAgentClass()) {
            case AgentGlobal.WECHAT_AGENT_CLASS:
                return new WechatAgent(agentParam);
            case AgentGlobal.QQ_AGENT_CLASS:
                return new QQAgent(agentParam);
            case AgentGlobal.DING_AGENT_CLASS:
                return new DingAgent(agentParam);
            case AgentGlobal.WEIBO_AGENT_CLASS:
                return new WeiboAgent(agentParam);
            case AgentGlobal.WHATSAPP_AGENT_CLASS:
                return new WhatsappAgent(agentParam);
            case AgentGlobal.LINE_AGENT_CLASS:
                return new LineAgent(agentParam);
            default:
                return null;
        }
    }
}
