package ai.servlet;

import ai.agent.pojo.GetAppListResponse;
import ai.agent.service.RpaService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RpaServlet extends BaseServlet {
    private final RpaService rpaService = new RpaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("getAppList")) {
            this.getAppList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
    }

    private void getAppList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        GetAppListResponse response = rpaService.getAppTypeList();
        if (response.getStatus().equals("success") && response.getData().size() > 13) {
            response.setData(response.getData().subList(0, 13));
        }
        responsePrint(resp, gson.toJson(response));
    }
}
