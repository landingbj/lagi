package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KFCTextGenerationTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.ahfi.cn/api/kfcv50?type=json";
    private String token = "";

    public KFCTextGenerationTool() {
        init();
    }

    private void init() {
        name = "kfc_text_generate";
        toolInfo = ToolInfo.builder().name("kfc_text_generate")
                .description("这是一个疯狂星期四（KFC）文案生成工具，可以帮助您生成疯狂星期四的文案")
                .args(Lists.newArrayList()).build();
        register(this);
    }

    public KFCTextGenerationTool(String token) {
        this.token = token;
        init();
    }

    private String search() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, "", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        if (map == null) {
            return "查询失败";
        }
        Map<String, Object> data  = (Map<String, Object>)map.get("data");
//        Map<String, Object> data = gson.fromJson(dataStr, type);
        return StrUtil.format("{\"疯狂星期四文案\": \"{}\"}", data.get("copywriting"));
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search();
    }

    public static void main(String[] args) {
        KFCTextGenerationTool kfcTextGenerationTool = new KFCTextGenerationTool();
        String result = kfcTextGenerationTool.search();
        System.out.println(result);
    }

}
