package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import ai.utils.HttpUtil;
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
                .description("一个图片生成工具, 可以根据用户输入英文描述生成任何类型的图像 如: 风景画、任务画、头像等")
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
                url = HttpUtil.getBaseUrl(endpoint) + "/" + url;
                return "图片生成成功地址:"+url;
            }
        } catch (Exception ignored) {
        }
        return "生成图片工具生成失败";
    }
}
