package ai.pnps.social;

import ai.config.pojo.PnpConfig;
import ai.pnps.PnpGlobal;
import ai.pnps.pojo.PnpParam;
import ai.pnps.pojo.SocialPnpParam;


public class DingPnp extends SocialPnp {
    private static final String APP_TYPE = PnpGlobal.APP_TYPE_DING;

    public DingPnp(PnpConfig pnpConfig) {
        super(pnpConfig);
    }

    public DingPnp(PnpParam param) {
        super(APP_TYPE, (SocialPnpParam) param);
    }
}
