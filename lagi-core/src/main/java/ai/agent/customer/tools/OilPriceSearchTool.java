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
public class OilPriceSearchTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.istero.com/resource/oilprice";
    private String token = "";

    public OilPriceSearchTool() {
        init();
    }

    private void init() {
        name = "search_oil_price";
        toolInfo = ToolInfo.builder().name("search_oil_price")
                .description("这是一个油价查询工具可以帮助查询对应省份城油价")
                .args(Lists.newArrayList(ToolArg.builder()
                        .name("province").type("string").description("所需查询油价的省份")
                        .build())).build();
        register(this);
    }

    public OilPriceSearchTool(String token) {
        this.token = token;
        init();
    }

    private String search(String province) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", token);
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, "{\"province\":\"" + province + "\"}", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        Map<String, Object> data  = (Map<String, Object>)map.get("data");
//        Map<String, Object> data = gson.fromJson(dataStr, type);
        return StrUtil.format("{\"省份\": \"{}\"， \"98#油价\":\"{}\", \"95#油价\":\"{}\", \"92#油价\":\"{}\", \"0#柴油价格\":\"{}\", \"更新时间\":\"{}\"}",
                data.get("name"), data.get("p98"), data.get("p95"), data.get("p92"), data.get("p0"), data.get("update_time"));
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search((String) args.get("province"));
    }

    public static void main(String[] args) {
        String token = "xxx";
        OilPriceSearchTool weatherSearchTool = new OilPriceSearchTool(token);
        String result = weatherSearchTool.search("北京");
        System.out.println(result);
    }

}
