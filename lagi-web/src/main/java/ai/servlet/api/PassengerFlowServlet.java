package ai.servlet.api;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import ai.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PassengerFlowServlet extends BaseServlet {
    private static final Gson gson = new Gson();
    // 接口地址（需替换为实际地址）
    private static final String CV_SYSTEM_URL = "http://cv-system:8080/api/door/control"; // CV视频系统接口
    private static final String DATA_API_URL = "http://data-service:8080/api/realtime"; // 数据侧实时API
    private static final String MODEL_SERVICE_URL = "http://model-service:8080/api/passenger/correct"; // 大模型服务接口
    private static final String KAFKA_TOPIC = "passenger_flow_topic"; // Kafka主题
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/passenger_db"; // PostgreSQL数据库URL

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        Map<String, Object> responseMap = new HashMap<>();
        try {
            if (method.equals("sendDoorSignal")) {
                sendDoorSignal(req, resp);
            } else if (method.equals("receiveCVResult")) {
                receiveCVResult(req, resp);
            } else if (method.equals("queryPassengerFlow")) {
                queryPassengerFlow(req, resp);
            } else {
                responseMap.put("code", 1);
                responseMap.put("message", "无效的接口方法");
                responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("data", null);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responsePrint(resp, gson.toJson(responseMap));
            }
        } catch (Exception e) {
            log.error("处理请求时发生错误: {}", method, e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responsePrint(resp, gson.toJson(responseMap));
        }
    }

    /**
     * 发送开关门信号给CV视频系统
     */
    private void sendDoorSignal(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            JsonObject requestBody = gson.fromJson(req.getReader(), JsonObject.class);
            String busNo = requestBody.get("bus_no").getAsString();
            String cameraNo = requestBody.get("camera_no").getAsString();
            String action = requestBody.get("action").getAsString(); // open or close
            String timestamp = requestBody.get("timestamp").getAsString();

            JsonObject doorSignal = new JsonObject();
            doorSignal.addProperty("event", "open_close_door");
            JsonObject data = new JsonObject();
            data.addProperty("bus_no", busNo);
            data.addProperty("camera_no", cameraNo);
            data.addProperty("action", action);
            data.addProperty("timestamp", timestamp);
            doorSignal.add("data", data);

            HttpResponse response = HttpRequest.post(CV_SYSTEM_URL)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(doorSignal))
                    .execute();

            responseMap.put("code", response.isOk() ? 0 : 1);
            responseMap.put("message", response.isOk() ? "开关门信号发送成功" : "发送开关门信号失败");
            responseMap.put("status", response.isOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(response.isOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.error("发送开关门信号失败", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        responsePrint(resp, gson.toJson(responseMap));
    }

    /**
     * 接收CV视频系统识别结果并调用大模型修正
     */
    private void receiveCVResult(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            JsonObject cvResult = gson.fromJson(req.getReader(), JsonObject.class);
            String event = cvResult.get("event").getAsString();
            JsonObject data = cvResult.get("data").getAsJsonObject();

            // 调用大模型服务修正客流识别结果（@田峻钢提供）
            JsonObject correctedResult = correctPassengerFlow(data);

            // 双写结果到PostgreSQL和Kafka
            saveToPostgreSQL(correctedResult);
            sendToKafka(correctedResult);

            responseMap.put("code", 0);
            responseMap.put("message", "接收并处理CV识别结果成功");
            responseMap.put("status", HttpServletResponse.SC_OK);
            responseMap.put("data", correctedResult);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("处理CV识别结果失败", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        responsePrint(resp, gson.toJson(responseMap));
    }

    /**
     * 查询客流数据（支持按线路、班次、站点、时间等条件）
     */
    private void queryPassengerFlow(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            String lineId = req.getParameter("line_id"); // 线路ID，必选
            String shiftId = req.getParameter("shift_id"); // 班次ID，可选
            String stationId = req.getParameter("station_id"); // 站点ID，可选
            String date = req.getParameter("date"); // 日期，默认当天
            String time = req.getParameter("time"); // 时间，精确到分钟，可选

            // 构造查询条件
            JsonObject queryParams = new JsonObject();
            queryParams.addProperty("line_id", lineId);
            if (shiftId != null) queryParams.addProperty("shift_id", shiftId);
            if (stationId != null) queryParams.addProperty("station_id", stationId);
            if (date != null) queryParams.addProperty("date", date);
            if (time != null) queryParams.addProperty("time", time);

            // 调用数据侧实时API获取刷卡数据、GPS等（@张俊提供）
            JsonObject realTimeData = fetchRealTimeData(queryParams);

            // 构造OD矩阵和客流明细
            JsonObject odMatrix = buildODMatrix(realTimeData, queryParams);

            responseMap.put("code", 0);
            responseMap.put("message", "客流数据查询成功");
            responseMap.put("status", HttpServletResponse.SC_OK);
            responseMap.put("data", odMatrix);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("查询客流数据失败", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        responsePrint(resp, gson.toJson(responseMap));
    }

    /**
     * 调用大模型服务修正客流识别结果（@田峻钢提供）
     */
    private JsonObject correctPassengerFlow(JsonObject data) {
        // TODO: 调用大模型服务接口修正客流识别结果（@田峻钢）
        try {
            HttpResponse response = HttpRequest.post(MODEL_SERVICE_URL)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(data))
                    .execute();
            if (response.isOk()) {
                return gson.fromJson(response.body(), JsonObject.class);
            } else {
                log.error("调用大模型服务失败，状态码: {}", response.getStatus());
                return data; // 默认返回原数据
            }
        } catch (Exception e) {
            log.error("调用大模型服务异常", e);
            return data; // 默认返回原数据
        }
    }

    /**
     * 保存结果到PostgreSQL
     */
    private void saveToPostgreSQL(JsonObject data) {
        try (Connection conn = getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO passenger_flow (id, bus_no, camera_no, timestamp, direction, feature, image, box_x, box_y, box_w, box_h) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            String id = UUID.randomUUID().toString();
            stmt.setString(1, id);
            stmt.setString(2, data.get("bus_no").getAsString());
            stmt.setString(3, data.get("camera_no").getAsString());
            stmt.setString(4, data.get("timestamp").getAsString());

            JsonArray events = data.get("events").getAsJsonArray();
            for (int i = 0; i < events.size(); i++) {
                JsonObject event = events.get(i).getAsJsonObject();
                stmt.setString(5, event.get("direction").getAsString());
                stmt.setString(6, event.get("feature").getAsString());
                stmt.setString(7, event.get("image").getAsString());
                stmt.setInt(8, event.get("box_x").getAsInt());
                stmt.setInt(9, event.get("box_y").getAsInt());
                stmt.setInt(10, event.get("box_w").getAsInt());
                stmt.setInt(11, event.get("box_h").getAsInt());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("保存到PostgreSQL失败", e);
        }
    }

    /**
     * 发送结果到Kafka
     */
    private void sendToKafka(JsonObject data) {
        // TODO: 调用黄敬洲提供的Kafka双写接口
        try {
            // 模拟Kafka发送逻辑
            log.info("发送到Kafka主题: {}, 数据: {}", KAFKA_TOPIC, gson.toJson(data));
            // 实际实现需调用黄敬洲提供的Kafka生产者接口
        } catch (Exception e) {
            log.error("发送到Kafka失败", e);
        }
    }

    /**
     * 获取实时数据（刷卡数据、GPS等）
     */
    private JsonObject fetchRealTimeData(JsonObject queryParams) {
        // TODO: 调用张俊提供的实时API获取刷卡数据、GPS等
        try {
            HttpResponse response = HttpRequest.post(DATA_API_URL)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(queryParams))
                    .execute();
            if (response.isOk()) {
                return gson.fromJson(response.body(), JsonObject.class);
            } else {
                log.error("调用实时数据API失败，状态码: {}", response.getStatus());
                return new JsonObject();
            }
        } catch (Exception e) {
            log.error("调用实时数据API异常", e);
            return new JsonObject();
        }
    }

    /**
     * 构造OD矩阵和客 Uploading file...流明细
     */
    private JsonObject buildODMatrix(JsonObject realTimeData, JsonObject queryParams) {
        // 构造OD矩阵和客流明细的逻辑
        JsonObject odMatrix = new JsonObject();
        // 示例实现，实际需根据业务逻辑完善
        odMatrix.addProperty("line_id", queryParams.get("line_id").getAsString());
        odMatrix.add("real_time_data", realTimeData);
        // TODO: 实现OD矩阵具体逻辑，结合实时数据和CV识别结果
        return odMatrix;
    }

    /**
     * 获取数据库连接
     */
    private Connection getDatabaseConnection() throws SQLException {
        // TODO: 替换为实际的数据库连接池配置
        return java.sql.DriverManager.getConnection(DB_URL, "username", "password");
    }
}