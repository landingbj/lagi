package ai.pnps;

import ai.config.pojo.PnpConfig;
import ai.pnps.pojo.PnpParam;
import ai.pnps.pojo.SocialPnpParam;
import ai.pnps.social.*;

public class SocialPnpFactory {
    public static SocialPnp getPnp(PnpConfig pnpConfig, PnpParam pnpParam) {
        switch (pnpConfig.getDriver()) {
            case PnpGlobal.WECHAT_PNP_CLASS:
                return new WechatPnp(pnpParam);
            case PnpGlobal.QQ_PNP_CLASS:
                return new QQPnp(pnpParam);
            case PnpGlobal.DING_PNP_CLASS:
                return new DingPnp(pnpParam);
            case PnpGlobal.WEIBO_PNP_CLASS:
                return new WeiboPnp(pnpParam);
            case PnpGlobal.WHATSAPP_PNP_CLASS:
                return new WhatsappPnp(pnpParam);
            case PnpGlobal.LINE_PNP_CLASS:
                return new LinePnp(pnpParam);
            default:
                throw new RuntimeException("Pnp not found");
        }
    }

    public static SocialPnp getSocialPnp(SocialPnpParam pnpParam) {
        String appId = pnpParam.getAppId();
        switch (appId) {
            case PnpGlobal.APP_TYPE_WECHAT:
                return new WechatPnp(pnpParam);
            case PnpGlobal.APP_TYPE_QQ:
                return new QQPnp(pnpParam);
            case PnpGlobal.APP_TYPE_DING:
                return new DingPnp(pnpParam);
            case PnpGlobal.APP_TYPE_WEIBO:
                return new WeiboPnp(pnpParam);
            case PnpGlobal.APP_TYPE_WHATSAPP:
                return new WhatsappPnp(pnpParam);
            case PnpGlobal.APP_TYPE_LINE:
                return new LinePnp(pnpParam);
            default:
                throw new RuntimeException("Pnp not found");
        }
    }
}
