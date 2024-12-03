package ai.agent.social;

import ai.agent.AgentGlobal;
import ai.agent.Agent;
import ai.agent.exception.TerminateAgentException;
import ai.agent.pojo.*;
import ai.agent.service.RpaService;
import ai.utils.OkHttpUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class SocialAgent extends Agent<AgentData, AgentData> {
    private static final String SAAS_URL = AgentGlobal.SAAS_URL;
    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final RpaService rpaService = new RpaService();

    private final String appId;
    private final String username;
    private final String robotFlag;
    private final String timerFlag;
    private final String repeaterFlag;
    private final String guideFlag;
    private final SocialAgentParam param;

    protected SocialAgent(String appId, SocialAgentParam param) {
        this.appId = appId;
        this.username = param.getUsername();
        this.robotFlag = param.getRobotFlag();
        this.timerFlag = param.getTimerFlag();
        this.repeaterFlag = param.getRepeaterFlag();
        this.guideFlag = param.getGuideFlag();
        this.param = param;
    }

    public SocialAgentParam getParam() {
        return param;
    }

    public AgentData communicate(AgentData data){
        return null;
    }

    @Override
    public void connect() {
        long startTime = System.currentTimeMillis();
        long authTimeout = AgentGlobal.AUTH_TIMEOUT;
//        StatusResponse authStatusResponse = this.getAuthStatus();
//        while (authStatusResponse.getStatus() != AgentGlobal.LOGIN_SUCCESS
//                && System.currentTimeMillis() - startTime < authTimeout) {
//            sleep(AgentGlobal.SLEEP_INTERVAL);
//            authStatusResponse = this.getAuthStatus();
//        }
//        if (authStatusResponse.getStatus() != AgentGlobal.LOGIN_SUCCESS) {
//            throw new ConnectionTimeoutException();
//        }
    }

    @Override
    public void terminate() {
        RpaResponse response = rpaService.closeAccount(username);
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
//            throw new StartAgentException();
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
//            throw new StopAgentException();
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
        return rpaService.getLoginQrCode(appId, username);
    }

    public StatusResponse getAuthStatus() {
        return rpaService.getAuthStatus(appId, username);
    }

    public RpaResponse getLoginStatus() {
        return rpaService.getLoginStatus(appId, username);
    }

    public String getUsername() {
        return username;
    }


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
