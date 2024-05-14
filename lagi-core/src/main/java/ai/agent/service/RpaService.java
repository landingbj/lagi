package ai.agent.service;

import ai.agent.AgentGlobal;
import ai.agent.pojo.*;
import ai.utils.OkHttpUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RpaService {
    private static final String SAAS_URL = AgentGlobal.SAAS_URL;
//    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private static final Gson gson = new Gson();

    public StatusResponse getAuthStatus(String appId, String username) {
        String url = SAAS_URL + "/" + "getAuthStatus";
        return getStatusResponse(url, appId, username);
    }

    public StatusResponse getLoginStatus(String appId, String username) {
        String url = SAAS_URL + "/saas/" + "getLoginStatus";
        return getStatusResponse(url, appId, username);
    }

    private StatusResponse getStatusResponse(String url, String appId, String username) {
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
        params.put("username", username);
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, StatusResponse.class);
    }

    public GetLoginQrCodeResponse getLoginQrCode(String appId, String username) {
        String url = SAAS_URL + "/" + "getLoginQrCode";
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
        params.put("username", username);
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, GetLoginQrCodeResponse.class);
    }

    public RpaResponse closeAccount(String username) {
        String url = SAAS_URL + "/saas/closeAccount";
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        String json = null;
        try {
            json = OkHttpUtil.post(url, gson.toJson(params));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(json, RpaResponse.class);
    }

    public GetAppListResponse getAppTypeList() {
        String url = SAAS_URL + "/" + "getAppTypeListV2";
        Map<String, String> params = new HashMap<>();
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        GetAppListResponse response = gson.fromJson(json, GetAppListResponse.class);
        return response;
    }

    public ChannelUserResponse getChannelUser() {
        String url = SAAS_URL + "/" + "getChannelUser";
        Map<String, String> params = new HashMap<>();
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, ChannelUserResponse.class);
    }
}
