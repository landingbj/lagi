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
public class ChipQueryTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/chipquery/";

    public ChipQueryTool() {
        init();
    }

    private void init() {
        name = "chip_query";
        toolInfo = ToolInfo.builder().name("chip_query")
                .description("芯片查询工具，通过芯片名称或芯片ID查询芯片参数")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("keyword").type("string").description("芯片关键词")
                                .build(),
                        ToolArg.builder()
                                .name("get").type("integer").description("查询的芯片ID")
                                .build()))
                .build();
        register(this);
    }

    public String queryChip(String keyword, Integer get) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("keyword", keyword);
        if (get != null) {
            queryParams.put("get", String.valueOf(get));
        }

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

        Object dataObj = responseData.get("data");
        if (dataObj == null || !(dataObj instanceof List)) {
            return "未找到相关芯片信息";
        }

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;
        StringBuilder result = new StringBuilder();
        result.append("查询到以下芯片信息:\n");

        for (Map<String, Object> chip : dataList) {
            result.append("芯片名称: ").append(chip.get("name")).append("\n");
            result.append("描述: ").append(chip.get("descript")).append("\n");
            result.append("品牌: ").append(chip.get("brand_name")).append("\n\n");
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        Integer get = (Integer) args.get("get");
        return queryChip(keyword, get);
    }

    public static void main(String[] args) {
        ChipQueryTool chipQueryTool = new ChipQueryTool();
        String result = chipQueryTool.queryChip("ESP8266", null);
        System.out.println(result);
    }
}
