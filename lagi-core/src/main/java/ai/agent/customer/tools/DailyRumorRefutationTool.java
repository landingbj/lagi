package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Setter
public class DailyRumorRefutationTool extends AbstractTool {

//    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/zhihu/recommend/";

    private static final String API_ADDRESS = "https://api.istero.com/resource/zhihu/feeds/essence";

    private String token;

    public DailyRumorRefutationTool(String token) {
        init();
        this.token = token;
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
        if(token != null) {
            headers.put("Authorization", "Bearer " + token);
        }
        Map<String, String> queryParams = null;
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);

        if (response == null) {
            return "获取辟谣新闻失败";
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null || ((Double) map.get("code")).intValue() != 200) {
            return "获取辟谣新闻失败";
        }

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) map.get("data");
        if (dataList == null || dataList.isEmpty()) {
            return "今天没有新的辟谣新闻";
        }

        StringBuilder result = new StringBuilder("今日辟谣新闻:\n");
        dataList = dataList.stream().limit(1).collect(Collectors.toList());
        for (Map<String, Object> data : dataList) {
            String title = (String) data.get("title");
            String text = (String) data.getOrDefault("text", data.get("excerpt"));
            String url = (String) data.get("url");
            result.append("\n标题: ").append(title)
                  .append("\n内容: ").append(text)
                  .append("\n详情链接: ").append(url)
                  .append("\n---------------------------------\n");
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        return getDailyRumorRefutation();
    }

    public static void main(String[] args) {
        DailyRumorRefutationTool tool = new DailyRumorRefutationTool("a");
        String result = tool.getDailyRumorRefutation();
        System.out.println(result);
    }
}
