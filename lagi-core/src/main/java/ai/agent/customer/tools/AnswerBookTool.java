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
public class AnswerBookTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/answersbook/";

    public AnswerBookTool() {
        init();
    }

    private void init() {
        name = "answer_book";
        toolInfo = ToolInfo.builder().name("answer_book")
                .description("这是一个答案之书工具，可以根据问题获取相应的答案")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("question").type("string").description("需要查询的提问")
                                .build()))
                .build();
        register(this);
    }

    public String getAnswer(String question) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("question", question);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "查询失败";
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "查询失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败";
        }

        Map<String, Object> data = (Map<String, Object>) map.get("data");
        if (data == null) {
            return "没有找到相关答案";
        }

        String titleZh = (String) data.get("title_zh");
        String titleEn = (String) data.get("title_en");
        String descriptionZh = (String) data.get("description_zh");
        String descriptionEn = (String) data.get("description_en");

        return StrUtil.format("问题：{}\n答案：{}\n\n中文标题：{}\n英文标题：{}\n中文描述：{}\n英文描述：{}",
                question, titleZh, titleZh, titleEn, descriptionZh, descriptionEn);
    }

    @Override
    public String apply(Map<String, Object> args) {
        String question = (String) args.get("question");
        return getAnswer(question);
    }

    public static void main(String[] args) {
        AnswerBookTool answerBookTool = new AnswerBookTool();
        String result = answerBookTool.getAnswer("我现在应该去钓鱼吗？");
        System.out.println(result);
    }
}
