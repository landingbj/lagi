package ai.agent;

import ai.agent.pojo.AgentParam;
import ai.agent.pojo.SocialAgentParam;
import ai.agent.social.*;
import ai.config.pojo.AgentConfig;

public class AgentFactory {
    public static SocialAgent getAgent(AgentConfig agentConfig, AgentParam agentParam) {
        switch (agentConfig.getDriver()) {
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
                throw new RuntimeException("Agent not found");
        }
    }

    public static SocialAgent getSocialAgent(SocialAgentParam agentParam) {
        String appId = agentParam.getAppId();
        switch (appId) {
            case AgentGlobal.APP_TYPE_WECHAT:
                return new WechatAgent(agentParam);
            case AgentGlobal.APP_TYPE_QQ:
                return new QQAgent(agentParam);
            case AgentGlobal.APP_TYPE_DING:
                return new DingAgent(agentParam);
            case AgentGlobal.APP_TYPE_WEIBO:
                return new WeiboAgent(agentParam);
            case AgentGlobal.APP_TYPE_WHATSAPP:
                return new WhatsappAgent(agentParam);
            case AgentGlobal.APP_TYPE_LINE:
                return new LineAgent(agentParam);
            default:
                throw new RuntimeException("Agent not found");
        }
    }
}
