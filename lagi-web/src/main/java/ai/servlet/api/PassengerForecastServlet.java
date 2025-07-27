package ai.servlet.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import ai.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PassengerForecastServlet extends BaseServlet {
    private static final Gson gson = new Gson();
    private static final String FORECAST_URL = "http://localhost:8849/station/passenger/forecast/passengewayList";
    private static final String VALIDATE_URL = "http://localhost:8849/station/passenger/forecast/validate";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("passengerForecast")) {
            this.passengerForecast(req, resp);
        } else if (method.equals("passengerValidate")) {
            this.passengerValidate(req, resp);
        } else {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("code", 1);
            responseMap.put("message", "无效的接口方法");
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responsePrint(resp, gson.toJson(responseMap));
        }
    }

    private void passengerForecast(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            HttpResponse response = HttpRequest.get(FORECAST_URL)
                    .header("Content-Type", "application/json")
                    .execute();
            String responseBody = response.body();

            responseMap.put("code", response.isOk() ? 0 : 1);
            responseMap.put("message", response.isOk() ? "success" : "请求预测接口失败");
            responseMap.put("status", response.isOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", gson.fromJson(responseBody, Map.class));
            resp.setStatus(response.isOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.error("调用客流预测接口时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        responsePrint(resp, gson.toJson(responseMap));
    }

    private void passengerValidate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            HttpResponse response = HttpRequest.get(VALIDATE_URL)
                    .header("Content-Type", "application/json")
                    .execute();
            String responseBody = response.body();

            responseMap.put("code", response.isOk() ? 0 : 1);
            responseMap.put("message", response.isOk() ? "验证成功" : "请求验证接口失败");
            responseMap.put("status", response.isOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", gson.fromJson(responseBody, Map.class));
            resp.setStatus(response.isOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.error("调用客流验证接口时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        responsePrint(resp, gson.toJson(responseMap));
    }

}