package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Setter
public class DailyRumorRefutationTool extends AbstractTool {

    private static final String API_ADDRESS = "http://v.juhe.cn/toutiao/index";

    private String apiKey;

    public DailyRumorRefutationTool(String apiKey) {
        init();
        this.apiKey = apiKey;
    }

    private void init() {
        name = "daily_rumor_refutation";
        toolInfo = ToolInfo.builder().name("daily_rumor_refutation")
                .description("这是一个获取每日辟谣前线内容的工具，帮助用户查看最新的辟谣新闻")
                .args(Lists.newArrayList())
                .build();
        register(this);
    }

    public String getDailyRumorRefutation() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key", apiKey);
        queryParams.put("type", "shehui"); // 社会新闻分类，可能包含辟谣相关内容
        queryParams.put("page_size", "10"); // 获取10条新闻
        queryParams.put("is_filter", "1"); // 过滤广告

        String queryString = queryParams.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                    } catch (Exception e) {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));

        String fullUrl = API_ADDRESS + "?" + queryString;
        String response = ApiInvokeUtil.get(fullUrl, null, headers, 15, TimeUnit.SECONDS);

        if (response == null) {
            return "获取辟谣新闻失败";
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("result") == null) {
            return "获取辟谣新闻失败";
        }

        Map<String, Object> result = (Map<String, Object>) map.get("result");
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");

        if (dataList == null || dataList.isEmpty()) {
            return "今天没有新的辟谣新闻";
        }

        StringBuilder resultBuilder = new StringBuilder("今日辟谣新闻:\n");
        dataList = dataList.stream().limit(1).collect(Collectors.toList());
        for (Map<String, Object> data : dataList) {
            String title = (String) data.get("title");
            String text = (String) data.get("abstract");
            String url = (String) data.get("url");
            resultBuilder.append("\n标题: ").append(title)
                    .append("\n内容: ").append(text != null ? text : "无摘要")
                    .append("\n详情链接: ").append(url != null ? url : "无链接")
                    .append("\n---------------------------------\n");
        }

        return resultBuilder.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        return getDailyRumorRefutation();
    }

    public static void main(String[] args) {
        DailyRumorRefutationTool tool = new DailyRumorRefutationTool("bb6c94092e7bb81bcea88598c7e1047b");
        String result = tool.getDailyRumorRefutation();
        System.out.println(result);
    }
}
