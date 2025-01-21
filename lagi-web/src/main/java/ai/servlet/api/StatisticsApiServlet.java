package ai.servlet.api;

import ai.common.pojo.TraceAgentEntity;
import ai.common.pojo.TraceLlmEntity;
import ai.migrate.service.TraceService;
import ai.servlet.BaseServlet;
import ai.vector.pojo.VectorCollection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsApiServlet extends BaseServlet {
    private final TraceService traceService = new TraceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("llmHotRanking")) {
            llmHotRanking(req, resp);
        } else if (method.equals("agentHotRanking")) {
            agentHotRanking(req, resp);
        }
    }

    private void llmHotRanking(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        int limit = Integer.parseInt(req.getParameter("limit"));
        List<TraceLlmEntity> llmList;
        try {
            llmList = traceService.llmHotRanking(limit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> result = new HashMap<>();
        if (llmList == null) {
            result.put("status", "failed");
        } else {
            result.put("status", "success");
            result.put("data", llmList);
        }
        responsePrint(resp, toJson(result));
    }

    private void agentHotRanking(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        int limit = Integer.parseInt(req.getParameter("limit"));
        List<TraceAgentEntity> agentList = null;
        try {
            agentList = traceService.agentHotRanking(limit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> result = new HashMap<>();
        if (agentList == null) {
            result.put("status", "failed");
        } else {
            result.put("status", "success");
            result.put("data", agentList);
        }
        responsePrint(resp, toJson(result));
    }
}
