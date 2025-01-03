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
public class WebsitePingTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/website/ping.php";

    public WebsitePingTool() {
        init();
    }

    private void init() {
        name = "website_ping";
        toolInfo = ToolInfo.builder().name("website_ping")
                .description("这是一个网站Ping测试工具，可以测量到指定网站的延迟时间")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("url").type("string").description("需要测试延迟的URL")
                                .build()))
                .build();
        register(this);
    }

    public String getPing(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("url", url);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "测速失败";
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "测速失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "测速失败";
        }

        Map<String, Object> data = (Map<String, Object>) map.get("data");
        if (data == null) {
            return "无法获取Ping延迟数据";
        }

        String host = (String) data.get("host");
        String ip = (String) data.get("ip");
        String location = (String) data.get("location");
        String max = (String) data.get("max");
        String min = (String) data.get("min");
        String average = (String) data.get("average");

        return StrUtil.format("网站：{}\nIP地址：{}\n地点：{}\n最大延迟：{}\n最小延迟：{}\n平均延迟：{}",
                host, ip, location, max, min, average);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String url = (String) args.get("url");
        return getPing(url);
    }

    public static void main(String[] args) {
        WebsitePingTool websitePingTool = new WebsitePingTool();
        String result = websitePingTool.getPing("https://www.baidu.com/");
        System.out.println(result);
    }
}
