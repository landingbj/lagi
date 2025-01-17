package ai.migrate.service;

import ai.dto.AgentChargeDetail;
import ai.dto.PrepayRequest;
import ai.dto.PrepayResponse;
import ai.utils.MigrateGlobal;
import ai.utils.OkHttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PayService {
    private final Gson gson = new Gson();
    private static final String SAAS_BASE_URL = MigrateGlobal.SAAS_BASE_URL;

    public PrepayResponse h5Prepay(PrepayRequest prepayRequest) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", prepayRequest.getLagiUserId());
        params.put("agentId", prepayRequest.getAgentId().toString());
        params.put("fee", prepayRequest.getFee());
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/saas/api/weixin/h5AgentPrepay", params);
        PrepayResponse response = gson.fromJson(resultJson, PrepayResponse.class);
        return response;
    }

    public PrepayResponse prepay(PrepayRequest prepayRequest) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", prepayRequest.getLagiUserId());
        params.put("agentId", prepayRequest.getAgentId().toString());
        params.put("fee", prepayRequest.getFee());
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/saas/api/weixin/agentPrepay", params);
        PrepayResponse response = gson.fromJson(resultJson, PrepayResponse.class);
        return response;
    }

    public AgentChargeDetail getAgentChargeDetail(String outTradeNo) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", outTradeNo);
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/saas/api/weixin/getAgentChargeDetail", params);
        AgentChargeDetail response = gson.fromJson(resultJson, AgentChargeDetail.class);
        return response;
    }
}
