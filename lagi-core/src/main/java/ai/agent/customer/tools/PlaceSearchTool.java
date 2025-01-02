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
public class PlaceSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/baidumap/";

    public PlaceSearchTool() {
        init();
    }

    private void init() {
        name = "place_search";
        toolInfo = ToolInfo.builder().name("place_search")
                .description("地点搜索工具，通过地名检索相关地点及其基本信息")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("keyword").type("string").description("地点名称")
                                .build()))
                .build();
        register(this);
    }

    public String searchPlace(String keyword) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("keyword", keyword);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "检索失败，未获得响应数据";
        }

        Gson gson = new Gson();
        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, typeResponse);

        if (responseData == null || responseData.get("code") == null) {
            return "检索失败，返回数据无效";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "检索失败，返回状态不正常";
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) responseData.get("data");

        if (data == null || data.isEmpty()) {
            return "未找到相关地点信息";
        }

        StringBuilder result = new StringBuilder();
        result.append("地点名称: ").append(responseData.get("keyword")).append("\n");

        for (Map<String, Object> place : data) {
            String name = (String) place.get("name");
            String address = (String) place.get("address");
            String tag = (String) place.get("tag");
            String type = (String) place.get("type");

            result.append("名称: ").append(name)
                  .append("\n地址: ").append(address)
                  .append("\n标签: ").append(tag)
                  .append("\n类型: ").append(type)
                  .append("\n\n");
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        return searchPlace(keyword);
    }

    public static void main(String[] args) {
        PlaceSearchTool placeSearchTool = new PlaceSearchTool();
        String result = placeSearchTool.searchPlace("罗浮山");
        System.out.println(result);
    }
}
