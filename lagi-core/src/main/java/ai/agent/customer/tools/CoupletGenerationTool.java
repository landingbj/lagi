package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import lombok.Setter;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class CoupletGenerationTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/duilian/";

    public CoupletGenerationTool() {
        init();
    }

    private void init() {
        name = "couplet_generation";
        toolInfo = ToolInfo.builder().name("couplet_generation")
                .description("这是一个对联生成工具，可以根据上联生成下联")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("word").type("string").description("上联文本，如 '天增岁月人增寿'")
                                .build()))
                .build();
        register(this);
    }

    public String getCouplet(String word) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("word", word);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "生成失败";
        }
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(response, Map.class);

        if (map == null || map.get("code") == null || ((Double) map.get("code")).intValue() != 200) {
            return "生成失败";
        }

        Map<String, Object> data = (Map<String, Object>) map.get("data");
        String up = (String) data.get("up");
        String under = (String) data.get("under");

        return String.format("{\"上联\": \"%s\", \"下联\": \"%s\"}", up, under);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String word = (String) args.get("word");
        return getCouplet(word);
    }

    public static void main(String[] args) {
        CoupletGenerationTool coupletGenerationTool = new CoupletGenerationTool();
        String result = coupletGenerationTool.getCouplet("天增岁月人增寿");
        System.out.println(result);
    }
}
