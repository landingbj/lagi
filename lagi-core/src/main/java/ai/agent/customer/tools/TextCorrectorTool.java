package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Setter
public class TextCorrectorTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/textcorrector/";

    public TextCorrectorTool() {
        init();
    }

    private void init() {
        name = "text_corrector";
        toolInfo = ToolInfo.builder().name("text_corrector")
                .description("文本纠错工具，修正输入的文本中的拼写和语法错误")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("text").type("string").description("需要纠正的文本")
                                .build()))
                .build();
        register(this);
    }

    public String correctText(String inputText) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("text", inputText);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "纠错失败，未获得响应数据";
        }

        Gson gson = new Gson();
        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, typeResponse);

        if (responseData == null || responseData.get("code") == null) {
            return "纠错失败，返回数据无效";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "纠错失败，返回状态不正常";
        }

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");

        if (data == null || data.get("corrected_text") == null) {
            return "未找到纠正后的文本";
        }

        String correctedText = (String) data.get("corrected_text");
        return "纠正前: " + inputText + "\n纠正后: " + correctedText;
    }

    @Override
    public String apply(Map<String, Object> args) {
        String text = (String) args.get("text");
        return correctText(text);
    }

    public static void main(String[] args) {
        TextCorrectorTool textCorrectorTool = new TextCorrectorTool();
        String result = textCorrectorTool.correctText("我一经吃了很多药了，可是病还不好");
        System.out.println(result);
    }
}
