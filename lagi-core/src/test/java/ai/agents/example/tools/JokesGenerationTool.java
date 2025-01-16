package ai.agents.example.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.agent.customer.tools.AbstractTool;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JokesGenerationTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.v1.jixs.cc/api/wenan-duanzi/index.php";

    private Gson gson = new Gson();

    public JokesGenerationTool() {
        init();
    }

    private void init() {
        name = "jokes_generation";
        toolInfo = ToolInfo.builder().name("jokes_generation")
                .description("这是一个段子的生成工具,可以生成一篇搞笑段子")
                .args(Lists.newArrayList()).build();
        register(this);
    }

    private String search() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String post = ApiInvokeUtil.post(API_ADDRESS, headers, "", 15, TimeUnit.SECONDS);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(post, type);
        String formattedString = "段子生成失败!!!";
        try {
            if (map == null) {
                return "段子生成失败！";
            }

            if (map.get("code") != null && map.get("code").equals("获取段子成功")) {
                Map<String, Object> data  = (Map<String, Object>)map.get("data");
                formattedString = StrUtil.format("{\"生成的段子\": \"{}\"",
                        data.get("duanzi"));
                return formattedString;
            }
        }catch (Exception e){
            return "段子生成失败!!!!";
        }
        return formattedString;
    }
    @Override
    public String apply(Map<String, Object> stringObjectMap) {
        return search();
    }

    public static void main(String[] args) {
        JokesGenerationTool tool = new JokesGenerationTool();
        String result = tool.search();
        System.out.println(result);
    }


}
