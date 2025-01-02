package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
public class SparkDialogTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/xfai/";

    public SparkDialogTool() {
        init();
    }

    private void init() {
        name = "spark_dialog";
        toolInfo = ToolInfo.builder().name("spark_dialog")
                .description("与星火大模型进行对话，解决用户的基本需求")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("message").type("string").description("用户输入的消息")
                                .build()))
                .build();
        register(this);
    }

    public String dialog(String message) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("message", message);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "对话失败，未获得响应数据";
        }

        Gson gson = new Gson();
        Map<String, Object> responseData = gson.fromJson(response, Map.class);

        if (responseData == null || responseData.get("code") == null) {
            return "对话失败，返回数据无效";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "对话失败，返回状态不正常";
        }

        String answer = (String) responseData.get("answer");

        return answer != null ? answer : "无法理解您的问题，请再试一次。";
    }

    @Override
    public String apply(Map<String, Object> args) {
        String message = (String) args.get("message");
        return dialog(message);
    }

    public static void main(String[] args) {
        SparkDialogTool sparkDialogTool = new SparkDialogTool();
        String result = sparkDialogTool.dialog("你是谁");
        System.out.println(result);
    }
}
