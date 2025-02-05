package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class HotNewsTool extends AbstractTool {

    private static final String API_ADDRESS = "https://v.api.aa1.cn/api/topbaidu/";

    public HotNewsTool() {
        init();
    }

    private void init() {
        name = "hot_news";
        toolInfo = ToolInfo.builder().name("hot_news")
                .description("这是一个获取今日热点新闻的工具，可以帮助用户查询当前热门新闻")
                .args(Lists.newArrayList())
                .build();
        register(this);
    }

    private String removeScriptTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("(?i)<script[^>]*>(.|\n)*?</script>", "");
    }

    public String getHotNews() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, null, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "查询失败";
        }

        String cleanResponse = removeScriptTags(response);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(cleanResponse, type);

        if (map == null || map.get("code") == null) {
            return "查询失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败";
        }

        StringBuilder news = new StringBuilder();
        if (map.containsKey("newslist")) {
            List<Map<String, Object>> newsList = (List<Map<String, Object>>) map.get("newslist");
            for (Map<String, Object> newsItem : newsList) {
                String digest = (String) newsItem.get("digest");
                Double hotnum = (Double) newsItem.get("hotnum");
                news.append(StrUtil.format("摘要: {}, 热度: {}\n", digest, hotnum));
            }
        }

        return news.length() > 0 ? news.toString() : "暂无热点新闻";
    }

    @Override
    public String apply(Map<String, Object> args) {
        return getHotNews();
    }

    public static void main(String[] args) {
        HotNewsTool hotNewsTool = new HotNewsTool();
        String result = hotNewsTool.getHotNews();
        System.out.println(result);
    }
}