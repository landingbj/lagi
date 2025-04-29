package ai.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.common.pojo.VectorStoreConfig;
import ai.utils.MigrateGlobal;
import ai.vector.VectorStoreService;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;

import ai.common.pojo.Configuration;
import ai.migrate.service.UserService;
import com.google.gson.JsonObject;

public class UserServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    protected Gson gson = new Gson();
    private UserService userService = new UserService();
    private static Configuration config = MigrateGlobal.config;

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
        String userId = req.getParameter("userId");
        JsonObject data = new JsonObject();
        String category;
        VectorStoreConfig vectorStoreConfig = new VectorStoreService().getVectorStoreConfig();
        if (vectorStoreConfig == null) {
            category = null;
        } else {
            category = vectorStoreConfig.getDefaultCategory();
        }
        if (category == null) {
            if (currentCategory.isEmpty()) {
                category = userService.getRandomCategory();
            } else {
                category = currentCategory;
            }
        }
        if(StrUtil.isNotBlank(userId)) {
            category = category + "_" + userId;
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
        map.put("data", config.getSystemTitle());
        responsePrint(resp, gson.toJson(map));
    }
}
