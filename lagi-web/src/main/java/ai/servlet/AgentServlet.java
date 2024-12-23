package ai.servlet;

import ai.common.pojo.Response;
import ai.config.pojo.AgentConfig;
import ai.migrate.service.AgentService;
import ai.servlet.dto.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AgentServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    protected Gson gson = new Gson();
    private final AgentService agentService = new AgentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("getLagiAgentList")) {
            this.getLagiAgentList(req, resp);
        } else if (method.equals("getLagiAgent")) {
            this.getLagiAgent(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("addLagiAgent")) {
            this.addLagiAgent(req, resp);
        } else if (method.equals("updateLagiAgent")) {
            this.updateLagiAgent(req, resp);
        } else if (method.equals("deleteLagiAgentById")) {
            this.deleteLagiAgentById(req, resp);
        }
    }

    private void getLagiAgent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String lagiUserId = req.getParameter("lagiUserId");
        String agentId = req.getParameter("agentId");
        LagiAgentResponse lagiAgentResponse = agentService.getLagiAgent(lagiUserId, agentId);
        responsePrint(resp, gson.toJson(lagiAgentResponse));
    }

    private void getLagiAgentList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        String lagiUserId = req.getParameter("lagiUserId");
        LagiAgentListResponse lagiAgentResponse = agentService.getLagiAgentList(lagiUserId, pageNumber, pageSize);
        responsePrint(resp, gson.toJson(lagiAgentResponse));
    }

    private void addLagiAgent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        AgentConfig agentConfig = reqBodyToObj(req, AgentConfig.class);
        Response response = agentService.addLagiAgent(agentConfig);
        responsePrint(resp, gson.toJson(response));
    }

    private void updateLagiAgent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        AgentConfig agentConfig = reqBodyToObj(req, AgentConfig.class);
        Response response = agentService.updateLagiAgent(agentConfig);
        responsePrint(resp, gson.toJson(response));
    }

    private void deleteLagiAgentById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        Type listType = new TypeToken<ArrayList<Integer>>() {
        }.getType();
        List<Integer> ids = gson.fromJson(requestToJson(req), listType);
        Response response = agentService.deleteLagiAgentById(ids);
        responsePrint(resp, gson.toJson(response));
    }
}
