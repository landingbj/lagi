package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Setter
public class SurnameRankTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/bjx/";

    public SurnameRankTool() {
        init();
    }

    private void init() {
        name = "surname_rank";
        toolInfo = ToolInfo.builder().name("surname_rank")
                .description("这是一个查询姓氏在百家姓中的排名的工具")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("name").type("string").description("要查询的姓氏")
                                .build()))
                .build();
        register(this);
    }

    public String getSurnameRank(String surname) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("name", surname);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

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

        String name = (String) responseData.get("name");
        Object topObj = responseData.get("top");

        int top = 0;
        if (topObj instanceof Double) {
            top = ((Double) topObj).intValue();
        } else if (topObj instanceof Integer) {
            top = (Integer) topObj;
        }

        return String.format("姓氏: %s\n排名: 第%d位", name, top);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String surname = (String) args.get("name");
        return getSurnameRank(surname);
    }

    public static void main(String[] args) {
        SurnameRankTool surnameRankTool = new SurnameRankTool();
        String result = surnameRankTool.getSurnameRank("王");
        System.out.println(result);
    }
}
