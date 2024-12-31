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
public class BloodTypeCalculationTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/bloodtype/";

    public BloodTypeCalculationTool() {
        init();
    }

    private void init() {
        name = "blood_type_calculation";
        toolInfo = ToolInfo.builder().name("blood_type_calculation")
                .description("根据父母血型计算子代血型的可能性")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("father").type("string").description("父亲的血型")
                                .build(),
                        ToolArg.builder()
                                .name("mother").type("string").description("母亲的血型")
                                .build()))
                .build();
        register(this);
    }

    public String calculateBloodType(String father, String mother) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("father", father);
        queryParams.put("mother", mother);

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

        String fatherBloodType = (String) responseData.get("father");
        String motherBloodType = (String) responseData.get("mother");
        String possibleBloodTypes = (String) responseData.get("possible");
        String impossibleBloodTypes = (String) responseData.get("impossible");

        return String.format("父亲血型: %s\n母亲血型: %s\n子代可能血型: %s\n子代不可能血型: %s",
                fatherBloodType != null ? fatherBloodType : "无数据", 
                motherBloodType != null ? motherBloodType : "无数据", 
                possibleBloodTypes != null ? possibleBloodTypes : "无数据", 
                impossibleBloodTypes != null ? impossibleBloodTypes : "无数据");
    }

    @Override
    public String apply(Map<String, Object> args) {
        String father = (String) args.get("father");
        String mother = (String) args.get("mother");
        return calculateBloodType(father, mother);
    }

    public static void main(String[] args) {
        BloodTypeCalculationTool bloodTypeCalculationTool = new BloodTypeCalculationTool();
        String result = bloodTypeCalculationTool.calculateBloodType("B", "AB");
        System.out.println(result);
    }
}
