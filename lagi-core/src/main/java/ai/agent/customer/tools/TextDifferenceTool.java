package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
public class TextDifferenceTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/textdifference/";

    public TextDifferenceTool() {
        init();
    }

    private void init() {
        name = "text_difference";
        toolInfo = ToolInfo.builder().name("text_difference")
                .description("文本对比工具，计算两个文本的相似度并找出差异部分")
                .args(java.util.Arrays.asList(
                        ToolArg.builder()
                                .name("text1").type("string").description("第一个文本")
                                .build(),
                        ToolArg.builder()
                                .name("text2").type("string").description("第二个文本")
                                .build()))
                .build();
        register(this);
    }

    public String compareTexts(String text1, String text2) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("text1", text1);
        queryParams.put("text2", text2);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);

        if (response == null) {
            return "对比失败，未获得响应数据";
        }

        Gson gson = new Gson();
        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseData = gson.fromJson(response, typeResponse);

        if (responseData == null || responseData.get("code") == null) {
            return "对比失败，返回数据无效";
        }

        Object codeObj = responseData.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "对比失败，返回状态不正常";
        }

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");

        if (data == null || data.get("similarity") == null || data.get("missing") == null || data.get("add") == null) {
            return "未能获取到文本对比结果";
        }

        String similarityStr = (String) data.get("similarity");
        List<String> missing = (List<String>) data.get("missing");
        List<String> add = (List<String>) data.get("add");

        double similarity = Double.parseDouble(similarityStr.replace("%", "")); // 去掉百分号
        similarity = similarity * 100;

        String result = String.format("相似度：%.2f%%\n丢失部分：%s\n添加部分：%s", similarity, missing, add);
        return result;
    }

    @Override
    public String apply(Map<String, Object> args) {
        String text1 = (String) args.get("text1");
        String text2 = (String) args.get("text2");
        return compareTexts(text1, text2);
    }

    public static void main(String[] args) {
        TextDifferenceTool textDifferenceTool = new TextDifferenceTool();
        String result = textDifferenceTool.compareTexts("奔赴新的远征", "踏上新的征程");
        System.out.println(result);
    }
}
