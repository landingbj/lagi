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
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, "{\"city\":\"" + city + "\"}", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询天气信息失败";
        }
        if(((Double)map.get("code")).intValue() != 200) {
            return "查询天气信息失败";
        }
        Map<String, Object> data = (Map<String, Object>)map.get("data");
        return StrUtil.format("{province}-{city} 天气:{weather} 温度:{temperature}, 湿度:{humidity}, 风力:{windpower}, 风向：{winddirection}， 更新时间{update_time}", data );
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
