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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class CarQueryTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/car/";

    public CarQueryTool() {
        init();
    }

    private void init() {
        name = "car_query";
        toolInfo = ToolInfo.builder().name("car_query")
                .description("这是一个车辆查询工具，可以帮助用户查询车辆的价格、配置和封面信息")
                .args(Arrays.asList(  // 使用 Arrays.asList() 替代 List.of()
                        ToolArg.builder()
                                .name("search").type("string").description("要查询的车型名称")
                                .build()))
                .build();
        register(this);
    }

    public String getCarQueryInfo(String search) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", search);

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

        List<Map<String, Object>> data = (List<Map<String, Object>>) responseData.get("data");
        if (data == null || data.isEmpty()) {
            return "没有找到相关车辆信息";
        }

        StringBuilder result = new StringBuilder();
        result.append("车型: ").append(search).append("\n");

        for (Map<String, Object> car : data) {
            String carName = (String) car.get("car_name");
            String price = (String) car.get("price");
            String dealerPrice = (String) car.get("dealer_price");
            List<String> tags = (List<String>) car.get("tags");
            String coverUrl = (String) car.get("cover_url");

            result.append("\n").append(carName)
                    .append("\n价格: ").append(price)
                    .append("\n经销商价格: ").append(dealerPrice)
                    .append("\n标签: ").append(String.join(", ", tags))
                    .append("\n封面: ").append(coverUrl)
                    .append("\n");
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        String search = (String) args.get("search");
        return getCarQueryInfo(search);
    }

    public static void main(String[] args) {
        CarQueryTool carQueryTool = new CarQueryTool();
        String result = carQueryTool.getCarQueryInfo("小米SU7");
        System.out.println(result);
    }
}
