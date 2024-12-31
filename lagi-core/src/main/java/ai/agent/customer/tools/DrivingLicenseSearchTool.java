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
public class DrivingLicenseSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/drvinglicense/";

    public DrivingLicenseSearchTool() {
        init();
    }

    private void init() {
        name = "driving_license_search";
        toolInfo = ToolInfo.builder().name("driving_license_search")
                .description("这是一个查询驾考题库问题的工具")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("keyword").type("string").description("要搜索的题目内容")
                                .build()))
                .build();
        register(this);
    }

    public String searchDrivingLicenseQuestion(String keyword) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("keyword", keyword);

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

        List<Map<String, Object>> data = (List<Map<String, Object>>) responseData.get("data");
        if (data == null || data.isEmpty()) {
            return "没有找到相关题目";
        }

        Map<String, Object> questionData = data.get(0);

        String question = (String) questionData.get("question");
        String subject = (String) questionData.get("subject");
        String explain = (String) questionData.get("explain");
        List<String> options = (List<String>) questionData.get("option");
        String answer = (String) questionData.get("answer");

        String optionsStr = options != null ? String.join(", ", options) : "无选项";

        return String.format("题目: %s\n科目: %s\n选项: %s\n正确答案: %s\n解析: %s",
                question != null ? question : "无题目",
                subject != null ? subject : "无科目",
                optionsStr,
                answer != null ? answer : "无答案",
                explain != null ? explain : "无解析");
    }

    @Override
    public String apply(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        return searchDrivingLicenseQuestion(keyword);
    }

    public static void main(String[] args) {
        DrivingLicenseSearchTool drivingLicenseSearchTool = new DrivingLicenseSearchTool();
        String result = drivingLicenseSearchTool.searchDrivingLicenseQuestion("驾驶员在高速公路上行驶时，车辆左前轮突然爆胎，须第一时间紧握转向盘，然后轻踏制动踏板进行减速，并将车停靠在紧急停车带上。这样做的原因是什么？");
        System.out.println(result);
    }
}
