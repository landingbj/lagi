package ai.worker.chengtouyun;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import okhttp3.*;
import java.io.IOException;

public class SseHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        String sseUrl = "你的流式接口URL"; // 替换为实际的接口URL

        Request request = new Request.Builder()
                .url(sseUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response: " + response);
                }

                // 处理流式响应
                processStream(response);
            }
        });
    }

    private static void processStream(Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            return;
        }

        try (var source = body.source()) {
            String currentId = null;
            String currentEvent = null;
            StringBuilder currentData = new StringBuilder();

            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) {
                    break;
                }

                // 空行表示一个事件的结束
                if (line.isEmpty()) {
                    if (currentEvent != null) {
                        // 处理完整的事件
                        handleEvent(currentId, currentEvent, currentData.toString());
                    }
                    // 重置事件数据
                    currentId = null;
                    currentEvent = null;
                    currentData.setLength(0);
                    continue;
                }

                // 解析行数据
                if (line.startsWith("id: ")) {
                    currentId = line.substring(4).trim();
                } else if (line.startsWith("event: ")) {
                    currentEvent = line.substring(7).trim();
                } else if (line.startsWith("data: ")) {
                    currentData.append(line.substring(6));
                }
            }
        }
    }

    private static void handleEvent(String id, String event, String data) {
        try {
            System.out.println("===== 新事件 =====");
            System.out.println("ID: " + id);
            System.out.println("Event: " + event);

            // 解析data为JSON对象
            JsonNode dataNode = objectMapper.readTree(data);
            System.out.println("Text: " + dataNode.get("text").asText());
            System.out.println("Turns: " + dataNode.get("turns"));

            // 处理params（如果有）
            JsonNode paramsNode = dataNode.get("params");
            if (paramsNode != null && !paramsNode.isNull()) {
                System.out.println("Params: " + paramsNode.toString());

                // 如果有memory_info，单独处理
                if (paramsNode.has("memory_info")) {
                    JsonNode memoryNode = objectMapper.readTree(paramsNode.get("memory_info").asText());
                    System.out.println("Memory Name: " + memoryNode.get("name").asText());
                }
            }
        } catch (Exception e) {
            System.err.println("处理事件出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
