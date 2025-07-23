package ai.worker.chengtouyun;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Xinjiob {
    // 配置信息
    private static final String SECRET_KEY = "b9ac29bd97ff45d294e6b4a299b087f9";
    private static final String VEHICLE_PAGE_URL = "/worker/vehiclepagedetailsv";
    private static final String TYRE_DETAIL_URL = "/worker/tiredetail";
    private static final int CURRENT_PAGE = 13;
    private static final int PAGE_SIZE = 100;
    private static final String CSV_FILENAME = "C:\\temp\\tyre_details13.csv";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("开始获取车辆列表...");
        List<String> busIds = getBusIds();
        if (busIds.isEmpty()) {
            System.out.println("未获取到任何busId，程序退出");
            return;
        }
        System.out.printf("成功获取 %d 个busId%n", busIds.size());

        System.out.println("开始获取轮胎详情并实时写入CSV...");
        int totalWritten = 0;
        boolean isFirstBatch = true;

        for (int i = 0; i < busIds.size(); i++) {
            String busId = busIds.get(i);
            System.out.printf("正在处理第 %d/%d 个busId：%s%n", i + 1, busIds.size(), busId);

            List<Map<String, Object>> tyreData = getTyreDetails(busId);
            System.out.printf("当前获取到 %d 条轮胎数据%n", tyreData.size());

            int writtenCount = writeToCsv(tyreData, isFirstBatch);
            totalWritten += writtenCount;
            System.out.printf("已写入 %d 条数据，累计写入 %d 条数据%n", writtenCount, totalWritten);

            isFirstBatch = false;
            try {
                Thread.sleep(1000); // 间隔1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("线程被中断：" + e.getMessage());
                break;
            }
        }

        System.out.printf("所有处理完成，共写入 %d 条记录到 %s%n", totalWritten, CSV_FILENAME);
    }

    /**
     * 使用Hutool的HttpRequest获取车辆分页列表中的busId集合
     */
    private static List<String> getBusIds() {
        try {
            // 构建请求URL和参数
            String url = "http://localhost:8080" + VEHICLE_PAGE_URL;

            // 使用Hutool发送GET请求
            HttpResponse response = HttpRequest.get(url)
                    .form("currentPage", CURRENT_PAGE)
                    .form("pageSize", PAGE_SIZE)
                    .timeout(10000) // 10秒超时
                    .execute();

            // 解析响应
            String responseBody = response.body();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if ("0".equals(rootNode.get("code").asText())
                    && rootNode.has("data")
                    && rootNode.get("data").has("records")) {

                JsonNode recordsNode = rootNode.get("data").get("records");
                List<String> busIds = new ArrayList<>();
                for (JsonNode record : recordsNode) {
                    if (record.has("busId")) {
                        busIds.add(record.get("busId").asText());
                    }
                }
                return busIds;
            } else {
                String message = rootNode.has("message") ? rootNode.get("message").asText() : "未知错误";
                System.out.printf("获取车辆列表失败：%s%n", message);
                return Collections.emptyList();
            }

        } catch (Exception e) {
            System.out.printf("获取busId时发生错误：%s%n", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 使用Hutool的HttpRequest获取指定busId的轮胎详情
     */
    private static List<Map<String, Object>> getTyreDetails(String busId) {
        try {
            String url = "http://localhost:8080" + TYRE_DETAIL_URL;

            // 使用Hutool发送GET请求
            HttpResponse response = HttpRequest.get(url)
                    .form("busId", busId)
                    .timeout(10000) // 10秒超时
                    .execute();

            // 解析JSON响应
            String responseBody = response.body();
            JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(responseBody);
            } catch (JsonProcessingException e) {
                System.out.printf("busId=%s的响应JSON解析失败：%s%n", busId, e.getMessage());
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("busId", busId);
                errorMap.put("error", "JSON解析失败");
                return Collections.singletonList(errorMap);
            }

            // 检查返回状态
            if (!"0".equals(rootNode.get("code").asText())) {
                String errorMsg = rootNode.has("message") ? rootNode.get("message").asText() : "接口返回非成功状态";
                System.out.printf("获取busId=%s的轮胎信息失败：%s%n", busId, errorMsg);
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("busId", busId);
                errorMap.put("error", errorMsg);
                return Collections.singletonList(errorMap);
            }

            // 处理数据字段
            if (!rootNode.has("data")) {
                System.out.printf("busId=%s的轮胎信息为空（data字段不存在）%n", busId);
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("busId", busId);
                errorMap.put("error", "无轮胎数据");
                return Collections.singletonList(errorMap);
            }

            JsonNode dataNode = rootNode.get("data");
            List<JsonNode> tyreNodes = new ArrayList<>();

            if (dataNode.isArray()) {
                dataNode.forEach(tyreNodes::add);
            } else {
                tyreNodes.add(dataNode);
            }

            // 转换为Map并添加busId
            List<Map<String, Object>> tyreList = new ArrayList<>();
            for (JsonNode tyreNode : tyreNodes) {
                Map<String, Object> tyreMap = objectMapper.convertValue(tyreNode, Map.class);
                tyreMap.put("busId", busId);
                tyreList.add(tyreMap);
            }

            return tyreList;

        } catch (Exception e) {
            System.out.printf("busId=%s的处理发生错误：%s%n", busId, e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("busId", busId);
            errorMap.put("error", "请求错误：" + e.getMessage());
            return Collections.singletonList(errorMap);
        }
    }

    /**
     * 写入CSV文件（保持原有逻辑）
     */
    private static int writeToCsv(List<Map<String, Object>> tyreData, boolean isFirst) {
        if (tyreData.isEmpty()) {
            return 0;
        }

        File csvFile = new File(CSV_FILENAME);
        boolean fileExists = csvFile.exists();
        boolean writeHeader = isFirst || !fileExists;

        // 收集所有字段
        Set<String> allFields = new TreeSet<>();
        tyreData.forEach(tyre -> allFields.addAll(tyre.keySet()));

        // 如果文件存在，读取已有表头
        if (fileExists && !isFirst) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                if (headerLine != null && !headerLine.isEmpty()) {
                    String[] existingHeaders = headerLine.split(",", -1);
                    allFields.addAll(Arrays.asList(existingHeaders));
                }
            } catch (IOException e) {
                System.out.printf("读取CSV表头失败：%s%n", e.getMessage());
            }
        }

        List<String> headers = new ArrayList<>(allFields);

        // 写入CSV
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile, true), StandardCharsets.UTF_8))) {

            // 写入表头
            if (writeHeader) {
                writer.write(String.join(",", headers));
                writer.newLine();
            }

            // 写入数据行
            for (Map<String, Object> tyre : tyreData) {
                List<String> rowValues = new ArrayList<>();
                for (String field : headers) {
                    Object value = tyre.getOrDefault(field, "");
                    if (value != null) {
                        rowValues.add(escapeCsvValue(value.toString()));
                    } else {
                        rowValues.add("无");
                    }
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
            }
            // 写入数据行
            for (Map<String, Object> tyre : tyreData) {
                List<String> rowValues = new ArrayList<>();
                for (String field : headers) {
                    Object value = tyre.getOrDefault(field, "");
                    if (value != null) {
                        rowValues.add(escapeCsvValue(value.toString()));
                    } else {
                        rowValues.add("无");
                    }
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
            }
            return tyreData.size();

        } catch (IOException e) {
            System.out.printf("写入CSV失败：%s%n", e.getMessage());
            return 0;
        }
    }

    /**
     * CSV特殊字符转义处理
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return value;
    }
}
