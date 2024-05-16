package ai.agent;

public class AgentGlobal {
    public static final String WECHAT_AGENT_CLASS = "ai.agent.social.WechatAgent";
    public static final String QQ_AGENT_CLASS = "ai.agent.social.QQAgent";
    public static final String DING_AGENT_CLASS = "ai.agent.social.DingAgent";
    public static final String WEIBO_AGENT_CLASS = "ai.agent.social.WeiboAgent";
    public static final String WHATSAPP_AGENT_CLASS = "ai.agent.social.WhatsappAgent";
    public static final String LINE_AGENT_CLASS = "ai.agent.social.LineAgent";

    public static final String APP_TYPE_WECHAT = "1";
    public static final String APP_TYPE_QQ = "2";
    public static final String APP_TYPE_DING = "3";
    public static final String APP_TYPE_WEIBO = "4";
    public static final String APP_TYPE_WHATSAPP = "8";
    public static final String APP_TYPE_LINE = "9";
    public static final String APP_TYPE_TIKTOK = "10";
    public static final String APP_TYPE_KUAISHOU = "11";

    public static final int RPA_QR_STATUS_INIT = 0;
    public static final int RPA_QR_STATUS_SCAN = 1;
    public static final int RPA_QR_STATUS_AUTH = 2;
    public static final int RPA_QR_STATUS_MANUAL = 3;
    public static final int RPA_QR_STATUS_DONE = 4;
    public static final int RPA_QR_STATUS_ERROR = 5;

    public static final int FAILED = 0;
    public static final int GET_QR_CODE_SUCCESS = 10;
    public static final int NUMBER_CODE_APP = 21;
    public static final int GET_QR_CODE_NEEDED = 11;
    public static final int LOGIN_MOBILE_AUTH = 30;
    public static final int FAILURE_PASSWORD = 90;
    public static final int FAILURE_VERIFICATION_CODE = 91;
    public static final int LOGIN_SUCCESS = 100;

    public static final long SLEEP_INTERVAL = 2000;
    public static final long AUTH_TIMEOUT = 3 * 60 * 1000;

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failed";

    public static final String ENABLE_FLAG = "1";
    public static final String DISABLE_FLAG = "0";

    public static final String SAAS_URL = "https://saas.landingbj.com";
//    public static final String SAAS_URL = "http://localhost:8090";
}
