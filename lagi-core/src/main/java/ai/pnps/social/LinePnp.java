package ai.pnps.social;

import ai.config.pojo.PnpConfig;
import ai.pnps.PnpGlobal;
import ai.pnps.pojo.PnpParam;
import ai.pnps.pojo.SocialPnpParam;


public class LinePnp extends SocialPnp {
    private static final String APP_TYPE = PnpGlobal.APP_TYPE_LINE;

    public LinePnp(PnpConfig pnpConfig) {
        super(pnpConfig);
    }

    public LinePnp(PnpParam param) {
        super(APP_TYPE, (SocialPnpParam) param);
    }
}
