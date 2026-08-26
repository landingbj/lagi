package ai.migrate.service;

import ai.dto.ChargeDetail;
import ai.dto.CreditUserBalance;
import ai.dto.PrepayRequest;
import ai.dto.PrepayResponse;
import ai.utils.AiGlobal;
import ai.utils.OkHttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreditService {
    private final Gson gson = new Gson();
    private static final String SAAS_BASE_URL = AiGlobal.SAAS_URL;
    private final BoxLandingAccountResolver boxAccountResolver;

    public CreditService() {
        this(new BoxLandingAccountResolver());
    }

    CreditService(BoxLandingAccountResolver boxAccountResolver) {
        this.boxAccountResolver = boxAccountResolver;
    }

    public PrepayResponse prepay(PrepayRequest prepayRequest) throws IOException {
        if (prepayRequest == null || isBlank(prepayRequest.getFee())) {
            throw new IllegalArgumentException("fee is required");
        }
        try {
            if (new BigDecimal(prepayRequest.getFee().trim()).compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("fee must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("fee must be a valid amount");
        }
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", boxAccountResolver.resolveAccountId());
        params.put("fee", prepayRequest.getFee());
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/saas/api/console_pay/prepay", params);
        PrepayResponse response = gson.fromJson(resultJson, PrepayResponse.class);
        return response;
    }

    public List<ChargeDetail> getSharedChargeDetails() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("userId", boxAccountResolver.resolveAccountId());
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/saas/api/console_pay/getChargeDetailByUserId", params);
        List<ChargeDetail> response = gson.fromJson(resultJson, new TypeToken<List<ChargeDetail>>() {
        }.getType());
        return response;
    }

    public CreditUserBalance getSharedBalance() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("userId", boxAccountResolver.resolveAccountId());
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/saas/api/console_pay/getUserBalance", params);
        CreditUserBalance response = gson.fromJson(resultJson, CreditUserBalance.class);
        return response;
    }

    public ChargeDetail getSharedChargeDetailBySeq(String seq) throws IOException {
        if (isBlank(seq)) {
            throw new IllegalArgumentException("seq is required");
        }
        String accountId = boxAccountResolver.resolveAccountId();
        Map<String, String> params = new HashMap<>();
        params.put("seq", seq);
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/saas/api/console_pay/getChargeDetailBySeq", params);
        ChargeDetail response = gson.fromJson(resultJson, ChargeDetail.class);
        if (response != null && !accountId.equals(response.getUserId())) {
            return null;
        }
        return response;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
