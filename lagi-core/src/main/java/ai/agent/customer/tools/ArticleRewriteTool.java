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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class ArticleRewriteTool extends AbstractTool {

    private static final String API_ADDRESS = "https://zj.v.api.aa1.cn/api/wzxx/";

    public ArticleRewriteTool() {
        init();
    }

    private void init() {
        name = "article_rewrite";
        toolInfo = ToolInfo.builder().name("article_rewrite")
                .description("这是一个文章续写工具，可以根据输入的文本生成新的段落")
                .args(Lists.newArrayList(
                        ToolArg.builder()
                                .name("msg").type("string").description("用户输入的文章内容")
                                .build()))
                .build();
        register(this);
    }

    public String getArticleRewrite(String input) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("msg", input);
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "续写失败";
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "续写失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 0) {
            return "续写失败";
        }

        Map<String, Object> data = (Map<String, Object>) map.get("data");
        List<String> generatedWriting = (List<String>) data.get("generate_writing");

        if (generatedWriting == null || generatedWriting.isEmpty()) {
            return "没有生成任何续写文本";
        }

        StringBuilder result = new StringBuilder();
        for (String text : generatedWriting) {
            result.append(text).append("\n");
        }

        return StrUtil.format("生成的文章段落：\n{}", result.toString());
    }

    @Override
    public String apply(Map<String, Object> args) {
        String msg = (String) args.get("msg");
        return getArticleRewrite(msg);
    }

    public static void main(String[] args) {
        ArticleRewriteTool articleRewriteTool = new ArticleRewriteTool();
        String result = articleRewriteTool.getArticleRewrite("我爱你");
        System.out.println(result);
    }
}
