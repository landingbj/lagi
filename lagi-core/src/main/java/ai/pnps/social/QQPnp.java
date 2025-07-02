package ai.pnps.social;

import ai.config.pojo.PnpConfig;
import ai.pnps.PnpGlobal;
import ai.pnps.pojo.PnpParam;
import ai.pnps.pojo.SocialPnpParam;


public class QQPnp extends SocialPnp {
    private static final String APP_TYPE = PnpGlobal.APP_TYPE_QQ;

    public QQPnp(PnpConfig pnpConfig) {
        super(pnpConfig);
    }

    public QQPnp(PnpParam param) {
        super(APP_TYPE, (SocialPnpParam) param);
    }
}
