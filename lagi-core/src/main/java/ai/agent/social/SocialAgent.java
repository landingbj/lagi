package ai.agent.social;

import ai.agent.Agent;
import ai.agent.AgentGlobal;
import ai.agent.exception.ConnectionTimeoutException;
import ai.agent.exception.StartAgentException;
import ai.agent.exception.StopAgentException;
import ai.agent.exception.TerminateAgentException;
import ai.agent.pojo.*;
import ai.utils.OkHttpUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class SocialAgent extends Agent {
    private static final String SAAS_URL = AgentGlobal.SAAS_URL;
    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final String appId;
    private final String username;
    private final String robotFlag;
    private final String timerFlag;
    private final String repeaterFlag;
    private final String guideFlag;

    protected SocialAgent(String appId, SocialAgentParam param) {
        this.appId = appId;
        this.username = param.getUsername();
        this.robotFlag = param.getRobotFlag();
        this.timerFlag = param.getTimerFlag();
        this.repeaterFlag = param.getRepeaterFlag();
        this.guideFlag = param.getGuideFlag();
    }

    @Override
    public void connect() {
        long startTime = System.currentTimeMillis();
        long authTimeout = AgentGlobal.AUTH_TIMEOUT;
        StatusResponse authStatusResponse = this.getAuthStatus();
        while (authStatusResponse.getStatus() != AgentGlobal.LOGIN_SUCCESS
                && System.currentTimeMillis() - startTime < authTimeout) {
            sleep(AgentGlobal.SLEEP_INTERVAL);
            authStatusResponse = this.getAuthStatus();
        }
        if (authStatusResponse.getStatus() != AgentGlobal.LOGIN_SUCCESS) {
            throw new ConnectionTimeoutException();
        }
    }

    @Override
    public void terminate() {
        String url = SAAS_URL + "/saas/closeAccount";
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        String json = null;
        try {
            json = OkHttpUtil.post(url, gson.toJson(params));
        } catch (IOException e) {
            e.printStackTrace();
        }
        RpaResponse response = gson.fromJson(json, RpaResponse.class);
        if (response.getStatus().equals("failed")) {
            throw new TerminateAgentException();
        }
    }

    @Override
    public void start() {
        String url = SAAS_URL + "/" + "startRpaFunction";
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
        params.put("username", username);
        params.put("robotFlag", robotFlag);
        params.put("timerFlag", timerFlag);
        params.put("repeaterFlag", repeaterFlag);
        params.put("guideFlag", guideFlag);
        String json = null;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RpaResponse response = gson.fromJson(json, RpaResponse.class);
        if (response.getStatus().equals("failed")) {
            throw new StartAgentException();
        }
    }

    @Override
    public void stop() {
        String url = SAAS_URL + "/" + "stopRpaFunction";
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId);
        params.put("username", username);
        String json = null;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RpaResponse response = gson.fromJson(json, RpaResponse.class);
        if (response.getStatus().equals("failed")) {
            throw new StopAgentException();
        }
    }

    @Override
    public void send(AgentData data) {
        String url = SAAS_URL + "/saas/putCompletionsResponse";
        String json = null;
        try {
            json = OkHttpUtil.post(url, gson.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        RpaResponse response = gson.fromJson(json, RpaResponse.class);
        if (response.getStatus().equals("failed")) {
            throw new TerminateAgentException();
        }
    }

    @Override
    public AgentData receive() {
        String url = SAAS_URL + "/" + "getCompletionsRequest";
        Map<String, String> params = new HashMap<>();
        params.put("channelUser", username);
        String json;
        try {
            json = OkHttpUtil.get(url, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(json, SocialReceiveData.class);
    }

    public GetLoginQrCodeResponse getLoginQrCode() {
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

    public StatusResponse getAuthStatus() {
        String url = SAAS_URL + "/" + "getAuthStatus";
        return getStatusResponse(url);
    }

    public StatusResponse getLoginStatus() {
        String url = SAAS_URL + "/saas/" + "getLoginStatus";
        return getStatusResponse(url);
    }

    public String getUsername() {
        return username;
    }

    private StatusResponse getStatusResponse(String url) {
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
