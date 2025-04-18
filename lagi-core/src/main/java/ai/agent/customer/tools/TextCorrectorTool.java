package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.LlmUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Collections;
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
//        Map<String, String> queryParams = new HashMap<>();
//        queryParams.put("text", inputText);
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//
//        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, java.util.concurrent.TimeUnit.SECONDS);
//
//        if (response == null) {
//            return "纠错失败，未获得响应数据";
//        }
//
//        Gson gson = new Gson();
//        Type typeResponse = new TypeToken<Map<String, Object>>() {}.getType();
//        Map<String, Object> responseData = gson.fromJson(response, typeResponse);
//
//        if (responseData == null || responseData.get("code") == null) {
//            return "纠错失败，返回数据无效";
//        }
//
//        Object codeObj = responseData.get("code");
//        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
//            return "纠错失败，返回状态不正常";
//        }
//
//        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
//
//        if (data == null || data.get("corrected_text") == null) {
//            return "未找到纠正后的文本";
//        }
//
//        String correctedText = (String) data.get("corrected_text");
//        return "纠正前: " + inputText + "\n纠正后: " + correctedText;
        ChatCompletionResult chatCompletionResult = LlmUtil.callLLm("你是一个文本纠正助手\n" +
                        "目标：\n" +
                        "\n" +
                        "你的任务是对用户输入的文本进行分析，并提供经过纠正后的版本。纠正内容包括但不限于语法错误、拼写错误、标点符号使用不当、句式结构问题以及语义模糊的地方。在修改过程中，请尽量保留用户的原始意图和语气风格。\n" +
                        "\n" +
                        "工作步骤：\n" +
                        "\n" +
                        "理解原文：仔细阅读用户提供的文本，确保完全理解其含义。\n" +
                        "检查错误：从语法、拼写、标点、逻辑连贯性等方面查找需要改进的地方。\n" +
                        "优化表达：如果发现冗长或不清晰的表述，可以适当简化或重新组织语言，但需尊重用户的原意。\n" +
                        "输出结果：将纠正后的文本呈现给用户，并简要说明修改的原因（如必要）。\n" +
                        "示例对话：\n" +
                        "\n" +
                        "用户输入：\n" +
                        "\n" +
                        "“我昨天去商店买了苹果，香蕉，和橙子但我忘记拿收据了。”\n" +
                        "系统回复：\n" +
                        "\n" +
                        "纠正后文本：\n" +
                        "\n" +
                        "“我昨天去商店买了苹果、香蕉和橙子，但我忘记拿收据了。”\n" +
                        "\n" +
                        "修改说明：\n" +
                        "在列举项目时，“和”前的逗号是多余的，应去掉（符合中文书写习惯）。\n" +
                        "“橙子”与“但”之间缺少逗号连接，导致句子不通顺，已添加逗号分隔。"
                , Collections.emptyList(), inputText);
        if (chatCompletionResult != null) {
            String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
            if (answer != null) {
                return answer;
            }
        }
        return "纠错失败";
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
