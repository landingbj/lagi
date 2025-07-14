package ai.servlet;

import ai.pnps.service.RpaService;
import ai.migrate.service.NextPromptService;
import ai.pnps.pojo.*;
import ai.servlet.dto.NextPromptRequest;
import ai.servlet.dto.NextPromptResponse;
import ai.servlet.dto.StandardTimeRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RpaServlet extends BaseServlet {
    private final RpaService rpaService = new RpaService();
    private final NextPromptService nextPromptService = new NextPromptService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("getAppList")) {
            this.getAppList(req, resp);
        } else if (method.equals("getLoginQrCode")) {
            this.getLoginQrCode(req, resp);
        } else if (method.equals("getLoginStatus")) {
            this.getLoginStatus(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("nextPrompt")) {
            this.nextPrompt(req, resp);
        } else if (method.equals("getStandardTime")) {
            this.getStandardTime(req, resp);
        } else if (method.equals("addTimerTask")) {
            this.addTimerTask(req, resp);
        } else if (method.equals("startRobot")) {
            this.startRobot(req, resp);
        }
    }

    private void getAppList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        GetAppListResponse response = rpaService.getAppTypeList();
        if (response.getStatus().equals("success") && response.getData().size() > 13) {
            response.setData(response.getData().subList(0, 13));
        }
        responsePrint(resp, gson.toJson(response));
    }

    private void getLoginQrCode(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String appId = req.getParameter("appId");
        String username = req.getParameter("username");
        GetLoginQrCodeResponse response = rpaService.getLoginQrCode(appId, username);
        responsePrint(resp, gson.toJson(response));
    }

    private void nextPrompt(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        NextPromptRequest nextPromptRequest = gson.fromJson(requestToJson(req), NextPromptRequest.class);
        NextPromptResponse response = nextPromptService.nextPrompt(nextPromptRequest);
        responsePrint(resp, gson.toJson(response));
    }

    private void getLoginStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String appId = req.getParameter("appId");
        String username = req.getParameter("username");
        RpaResponse response = rpaService.getLoginStatus(appId, username);
        responsePrint(resp, gson.toJson(response));
    }

    private void getStandardTime(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        StandardTimeRequest nextPromptRequest = gson.fromJson(requestToJson(req), StandardTimeRequest.class);
        String standardTime = rpaService.getStandardTime(nextPromptRequest.getPrompt());
//        String standardTime = "2024-08-01 03:10:20";
        Map<String, Object> map = new HashMap<>();
        if (standardTime != null) {
            map.put("status", "success");
            map.put("data", standardTime);
        } else {
            map.put("status", "failure");
        }
        responsePrint(resp, gson.toJson(map));
    }

    private void addTimerTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        AddTimerRequest request = gson.fromJson(requestToJson(req), AddTimerRequest.class);
        RpaResponse response = rpaService.addTimerTask(request);
        responsePrint(resp, gson.toJson(response));
    }

    private void startRobot(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        StartRobotRequest request = gson.fromJson(requestToJson(req), StartRobotRequest.class);
        boolean robotEnable = rpaService.startRobot(request);
        StartRobotResponse response = new StartRobotResponse();
        response.setStatus("success");
        response.setRobotEnable(robotEnable);
        responsePrint(resp, gson.toJson(response));
    }
}
