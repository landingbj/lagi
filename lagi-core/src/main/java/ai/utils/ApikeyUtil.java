package ai.utils;

import ai.common.pojo.Response;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApikeyUtil {
    private static final Gson gson = new Gson();

    public static boolean isApiKeyValid(String apiKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        boolean result = false;
        try {
            String json = OkHttpUtil.post(AiGlobal.SAAS_URL + "/isApiKeyValid", headers, new HashMap<>(), "");
            Response response = gson.fromJson(json, Response.class);
            if (response != null && "success".equals(response.getStatus())) {
                result = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
