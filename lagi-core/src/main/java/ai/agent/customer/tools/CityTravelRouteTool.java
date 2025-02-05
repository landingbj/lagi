package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class CityTravelRouteTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/citytravelroutes/";

    public CityTravelRouteTool() {
        init();
    }

    private void init() {
        name = "city_travel_route";
        toolInfo = ToolInfo.builder().name("city_travel_route")
                .description("这是一个城市出行路线工具，可以帮助用户查询出行路线的详细信息，包括距离、油耗、过路费、耗时等")
                .args(Arrays.asList(
                        ToolArg.builder()
                                .name("from").type("string").description("出发城市")
                                .build(),
                        ToolArg.builder()
                                .name("to").type("string").description("目的城市")
                                .build()))
                .build();
        register(this);
    }

    public String getCityTravelRoute(String from, String to) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("from", from);
        queryParams.put("to", to);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);

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

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data == null) {
            return "没有找到相关路线信息";
        }

        String corese = (String) data.get("corese");
        String distance = (String) data.get("distance");
        String time = (String) data.get("time");
        String fuelcosts = (String) data.get("fuelcosts");
        String bridgetoll = (String) data.get("bridgetoll");
        String totalcost = (String) data.get("totalcost");
        String roadconditions = (String) data.get("roadconditions");

        return String.format("路线: %s\n距离: %s\n耗时: %s\n油耗: %s\n过路费: %s\n总费用: %s\n路况: %s",
                corese, distance, time, fuelcosts, bridgetoll, totalcost, roadconditions);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String from = (String) args.get("from");
        String to = (String) args.get("to");
        return getCityTravelRoute(from, to);
    }

    public static void main(String[] args) {
        CityTravelRouteTool cityTravelRouteTool = new CityTravelRouteTool();
        String result = cityTravelRouteTool.getCityTravelRoute("武汉", "北京");
        System.out.println(result);
    }
}