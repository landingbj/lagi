package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class GoogleTranslateTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/googletranslate/";

    public GoogleTranslateTool() {
        init();
    }

    private void init() {
        name = "google_translate";
        toolInfo = ToolInfo.builder().name("google_translate")
                .description("这是一个智能翻译工具，支持自动中英文翻译")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("text").type("string").description("待翻译的文字")
                                .build(),
                        ToolArg.builder()
                                .name("type").type("string").description("翻译模式，auto为自动检测语言，en为英文转中文，zh为中文转英文")
                                .build()))
                .build();
        register(this);
    }

    public String getTranslation(String text, String type) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("text", text);
        queryParams.put("type", type != null ? type : "auto");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);

        if (response == null) {
            return "翻译失败";
        }

        Gson gson = new Gson();
        Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, typeOfMap);

        if (map == null || map.get("code") == null || ((Double) map.get("code")).intValue() != 200) {
            return "翻译失败";
        }

        String result = (String) map.get("result");
        return "翻译结果: " + result;
    }

    @Override
    public String apply(Map<String, Object> args) {
        String text = (String) args.get("text");
        String type = (String) args.get("type");
        return getTranslation(text, type);
    }

    public static void main(String[] args) {
        GoogleTranslateTool googleTranslateTool = new GoogleTranslateTool();
        String result = googleTranslateTool.getTranslation("HelloWorld", "auto");
        System.out.println(result);
    }
}
