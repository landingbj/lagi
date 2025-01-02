package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
public class BingSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/bingsearch/";

    public BingSearchTool() {
        init();
    }

    private void init() {
        name = "bing_search";
        toolInfo = ToolInfo.builder().name("bing_search")
                .description("通过Bing搜索引擎获取搜索结果")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("search").type("string").description("搜索关键词")
                                .build()))
                .build();
        register(this);
    }

    public String getBingSearchResults(String searchKeyword) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", searchKeyword);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "查询失败，未获得响应数据";
        }

        Gson gson = new Gson();
        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, typeResponse);

        if (responseData == null || responseData.get("code") == null) {
            return "查询失败，返回数据无效";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败，返回状态不正常";
        }

        List<Map<String, String>> results = (List<Map<String, String>>) responseData.get("data");

        if (results == null || results.isEmpty()) {
            return "未找到相关搜索结果";
        }

        StringBuilder resultBuilder = new StringBuilder();
        for (Map<String, String> result : results) {
            resultBuilder.append("标题: ").append(result.get("title")).append("\n")
                    .append("链接: ").append(result.get("href")).append("\n")
                    .append("简介: ").append(result.get("abstract")).append("\n\n");
        }

        return resultBuilder.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String searchKeyword = (String) args.get("search");
        return getBingSearchResults(searchKeyword);
    }

    public static void main(String[] args) {
        BingSearchTool bingSearchTool = new BingSearchTool();
        String result = bingSearchTool.getBingSearchResults("PearNo");
        System.out.println(result);
    }
}
