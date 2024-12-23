package ai.migrate.service;

import ai.common.pojo.Response;
import ai.config.pojo.AgentConfig;
import ai.servlet.dto.LagiAgentResponse;
import ai.servlet.dto.LoginResponse;
import ai.utils.MigrateGlobal;
import ai.utils.OkHttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentService {
    private final Gson gson = new Gson();
    private static final String SAAS_BASE_URL = MigrateGlobal.SAAS_BASE_URL;

    public Response addLagiAgent(AgentConfig agentConfig) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/addLagiAgent", gson.toJson(agentConfig));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public Response updateLagiAgent(AgentConfig agentConfig) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/updateLagiAgent", gson.toJson(agentConfig));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public Response deleteLagiAgentById(List<Integer> ids) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/deleteLagiAgentById", gson.toJson(ids));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public LagiAgentResponse getLagiAgentList(String lagiUserId, int pageNumber, int pageSize) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (lagiUserId != null) {
            params.put("lagiUserId", lagiUserId);
        }
        params.put("pageNumber", String.valueOf(pageNumber));
        params.put("pageSize", String.valueOf(pageSize));
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getLagiAgentList", params);
        LagiAgentResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentResponse.class);
        return lagiAgentResponse;
    }
}
