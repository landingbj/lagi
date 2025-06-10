package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class MealSuggestionTool extends AbstractTool {

//    private static final String API_ADDRESS = "https://zj.v.api.aa1.cn/api/eats/";

    private static final String API_ADDRESS = "https://api.istero.com/resource/v1/eat/what";

    private String token = "";


    public MealSuggestionTool(String token) {
        init();
        this.token = token;
    }

    private void init() {
        name = "meal_suggestion";
        toolInfo = ToolInfo.builder().name("meal_suggestion")
                .description("这是一个餐食建议工具，可以帮助用户选择午餐或晚餐")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("meal_type").type("string").description("想要查询的餐食类型，如 'meal1' 或 'meal2'")
                                .build()))
                .build();
        register(this);
    }

//    public String getMealSuggestion() {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("Authorization", "Bearer "+token);
//        Map<String, String> queryParams = new HashMap<>();
//        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 30, TimeUnit.SECONDS);
//        if (response == null) {
//            return "查询失败";
//        }
//        Gson gson = new Gson();
//        Type type = new TypeToken<Map<String, Object>>() {
//        }.getType();
//        Map<String, Object> map = gson.fromJson(response, type);
//
//        if (map == null || map.get("code") == null) {
//            return "查询失败";
//        }
//
//        Object codeObj = map.get("code");
//        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
//            return "查询失败";
//        }
//        String meal1 = (String) map.get("meal1");
//        String meal2 = (String) map.get("meal2");
//        String mealwhat = (String) map.get("mealwhat");
//
//        return StrUtil.format("{\"餐食选择1\": \"{}\", \"餐食选择2\": \"{}\", \"提示\": \"{}\"}", meal1, meal2, mealwhat);
//    }
//
    public String getMealSuggestion() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("token", token);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);
        if (response == null) {
            return "查询失败";
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "查询失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败";
        }
        Map<String, Object> data = (Map<String, Object>) map.get("data");

        return StrUtil.format("{\"餐食\": \"{}\"}", data.get("food"));
    }


    @Override
    public String apply(Map<String, Object> args) {
        return getMealSuggestion();
    }

    public static void main(String[] args) {
        String token = "";
        MealSuggestionTool tool = new MealSuggestionTool(token);
        String result = tool.apply(null);
        System.out.println(result);
    }
}
