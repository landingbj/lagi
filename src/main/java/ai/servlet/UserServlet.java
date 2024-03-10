package ai.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import ai.migrate.pojo.Configuration;
import ai.migrate.service.UserService;
import ai.utils.LagiGlobal;
import com.google.gson.JsonObject;

public class UserServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    protected Gson gson = new Gson();
    private UserService userService = new UserService();
    private static Configuration config = LagiGlobal.config;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("getRandomCategory")) {
            this.getRandomCategory(req, resp);
        } else if (method.equals("getDefaultTitle")) {
            this.getDefaultTitle(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private void getRandomCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String currentCategory = req.getParameter("currentCategory");
        JsonObject data = new JsonObject();
        String category = config.getDefault_category();
        if (category == null) {
            if (currentCategory.isEmpty()) {
                category = userService.getRandomCategory();
            } else {
                category = currentCategory;
            }
        }
        data.addProperty("category", category);
        Map<String, Object> map = new HashMap<>();
        if (category != null) {
            map.put("status", "success");
            map.put("data", data);
        } else {
            map.put("status", "failed");
        }
        responsePrint(resp, gson.toJson(map));
    }

    private void getDefaultTitle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("data", config.getSystem_title());
        responsePrint(resp, gson.toJson(map));
    }
}
