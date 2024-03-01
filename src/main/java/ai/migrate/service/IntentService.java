package ai.migrate.service;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;

public class IntentService {
    private static AiServiceCall call = new AiServiceCall();

    public String detectIntent(String prompt) {
        Object[] params = { prompt.trim() };
        String[] result = call.callWS(AiServiceInfo.WSKngUrl, "detectIntent", params);
        return result[0];
    }
}
