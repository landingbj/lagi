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
public class TextConversionTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/conversion/word.php";

    public TextConversionTool() {
        init();
    }

    private void init() {
        name = "text_conversion";
        toolInfo = ToolInfo.builder().name("text_conversion")
                .description("这是一个简体与繁体中文转换工具")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("text").type("string").description("需要转换的文本")
                                .build(),
                        ToolArg.builder()
                                .name("type").type("string").description("转换类型：简体转繁体(jzf) 或 繁体转简体(fzj)")
                                .build()))
                .build();
        register(this);
    }

    public String convertText(String text, String type) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("text", text);
        queryParams.put("type", type);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "转换失败";
        }
        Gson gson = new Gson();
        Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, typeToken);

        if (map == null || map.get("code") == null) {
            return "转换失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "转换失败";
        }

        String conversion = (String) map.get("conversion");
        String mode = (String) map.get("mode");

        return StrUtil.format("转换模式：{}\n转换结果：{}", mode, conversion);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String text = (String) args.get("text");
        String type = (String) args.get("type");
        return convertText(text, type);
    }

    public static void main(String[] args) {
        TextConversionTool textConversionTool = new TextConversionTool();
        String result = textConversionTool.convertText("随机繁体测试内容", "jzf");
        System.out.println(result);
    }
}
