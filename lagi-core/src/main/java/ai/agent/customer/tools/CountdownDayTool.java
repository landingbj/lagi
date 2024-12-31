package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class CountdownDayTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/countdownday/";

    public CountdownDayTool() {
        init();
    }

    private void init() {
        name = "countdown_day";
        toolInfo = ToolInfo.builder().name("countdown_day")
                .description("这是一个倒数日工具，可以帮助用户查询接下来的倒数日信息")
                .build();
        register(this);
    }

    public String getCountdownDay() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String response = ApiInvokeUtil.get(API_ADDRESS, null, headers, 15, TimeUnit.SECONDS);

        if (response == null) {
            return "查询失败";
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, type);

        if (responseData == null || responseData.get("code") == null) {
            return "查询失败";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败";
        }

        List<String> countdowns = (List<String>) responseData.get("data");
        if (countdowns == null || countdowns.isEmpty()) {
            return "没有倒数日数据";
        }

        StringBuilder result = new StringBuilder("即将到来的倒数日：\n");
        for (String countdown : countdowns) {
            result.append(countdown).append("\n");
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        return getCountdownDay();
    }

    public static void main(String[] args) {
        CountdownDayTool countdownDayTool = new CountdownDayTool();
        String result = countdownDayTool.getCountdownDay();
        System.out.println(result);
    }
}
