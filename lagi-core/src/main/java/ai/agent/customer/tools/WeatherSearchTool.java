package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.common.collect.Lists;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Setter
public class WeatherSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.istero.com/resource/weather/query";
    private String token = "";

    public WeatherSearchTool() {
        init();
    }

    private void init() {
        name = "search_weather";
        toolInfo = ToolInfo.builder().name("search_weather")
                .description("这是一个天气查询工具可以帮助查询对应城市天气")
                .args(Lists.newArrayList(ToolArg.builder().name("city").type("string").description("所需查询的城市").build())).build();
        register(this);
    }

    public WeatherSearchTool(String token) {
        this.token = token;
        init();
    }

    private String search(String city) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", token);
        return ApiInvokeUtil.post(API_ADDRESS, headers, "{\"city\":\"" + city + "\"}", 15, TimeUnit.SECONDS);
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search((String) args.get("city"));
    }

    public static void main(String[] args) {
        String token = "xxxx";
        WeatherSearchTool weatherSearchTool = new WeatherSearchTool(token);
        String result = weatherSearchTool.search("北京");
        System.out.println(result);
    }

}
