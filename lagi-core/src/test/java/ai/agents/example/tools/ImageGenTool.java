package ai.agents.example.tools;

import ai.agent.customer.pojo.ToolArg;
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

public class ImageGenTool extends AbstractTool {

    private final String endpoint;
    private Gson gson = new Gson();

    public ImageGenTool(String endpoint) {
        this.endpoint = endpoint;
        init();
    }

    private void init() {
        name = "image_generator";
        toolInfo = ToolInfo.builder().name("image_generator")
                .description("这是一个生成图片的工具可以根据输入的图片的英文描述的生成对应的图片")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("prompt").type("string").description("英文描述").build()
                )).build();
        register(this);
    }

    @Override
    public String apply(Map<String, Object> map) {
        String prompt = (String) map.get("prompt");
        try {
            String requestJson = StrUtil.format("{\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"content\": \"{}\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}", prompt);
            String post = ApiInvokeUtil.post(endpoint, null, requestJson, 30, TimeUnit.SECONDS);
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            Map<String, Object> res = gson.fromJson(post, type);
            String status = (String)res.get("status");
            if(!"failed".equals(status)){
                String url =  (String) res.get("result");
                return "图片生成成功地址:"+url;
            }
        } catch (Exception ignored) {
        }
        return "生成图片工具生成失败";
    }
}
