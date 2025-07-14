package ai.pnps.social;

import ai.config.pojo.PnpConfig;
import ai.pnps.PnpGlobal;
import ai.pnps.pojo.PnpParam;
import ai.pnps.pojo.SocialPnpParam;


public class WhatsappPnp extends SocialPnp {
    private static final String APP_TYPE = PnpGlobal.APP_TYPE_WHATSAPP;

    public WhatsappPnp(PnpConfig pnpConfig) {
        super(pnpConfig);
    }

    public WhatsappPnp(PnpParam param) {
        super(APP_TYPE, (SocialPnpParam) param);
    }
}
