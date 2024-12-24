package ai.migrate.service;

import ai.common.pojo.Response;
import ai.config.pojo.AgentConfig;
import ai.servlet.dto.LagiAgentExpenseListResponse;
import ai.servlet.dto.LagiAgentListResponse;
import ai.servlet.dto.LagiAgentResponse;
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

    public LagiAgentListResponse getLagiAgentList(String lagiUserId, int pageNumber, int pageSize, String publishStatus) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (lagiUserId != null) {
            params.put("lagiUserId", lagiUserId);
        }
        if (publishStatus != null) {
            params.put("publishStatus", publishStatus);
        }
        params.put("pageNumber", String.valueOf(pageNumber));
        params.put("pageSize", String.valueOf(pageSize));
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getLagiAgentList", params);
        LagiAgentListResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentListResponse.class);
        return lagiAgentResponse;
    }

    public LagiAgentResponse getLagiAgent(String lagiUserId, String agentId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", lagiUserId);
        params.put("agentId", agentId);
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getLagiAgent", params);
        LagiAgentResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentResponse.class);
        return lagiAgentResponse;
    }

    public LagiAgentExpenseListResponse getPaidAgentByUser(String lagiUserId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", lagiUserId);
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getPaidAgentByUser", params);
        LagiAgentExpenseListResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentExpenseListResponse.class);
        return lagiAgentResponse;
    }
}
