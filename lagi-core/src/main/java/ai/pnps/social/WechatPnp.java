package ai.pnps.social;

import ai.config.pojo.PnpConfig;
import ai.pnps.PnpGlobal;
import ai.pnps.pojo.PnpParam;
import ai.pnps.pojo.SocialPnpParam;


public class WechatPnp extends SocialPnp {
    private static final String APP_TYPE = PnpGlobal.APP_TYPE_WEIBO;

    public WechatPnp(PnpConfig pnpConfig) {
        super(pnpConfig);
    }

    public WechatPnp(PnpParam param) {
        super(APP_TYPE, (SocialPnpParam) param);
    }
}
