package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.LlmUtil;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.Collections;
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
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//        Map<String, String> queryParams = new HashMap<>();
//        queryParams.put("msg", input);
//        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
//        if (response == null) {
//            return "续写失败";
//        }
//        Gson gson = new Gson();
//        Type type = new TypeToken<Map<String, Object>>() {}.getType();
//        Map<String, Object> map = gson.fromJson(response, type);
//
//        if (map == null || map.get("code") == null) {
//            return "续写失败";
//        }
//
//        Object codeObj = map.get("code");
//        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 0) {
//            return "续写失败";
//        }
//
//        Map<String, Object> data = (Map<String, Object>) map.get("data");
//        List<String> generatedWriting = (List<String>) data.get("generate_writing");
//
//        if (generatedWriting == null || generatedWriting.isEmpty()) {
//            return "没有生成任何续写文本";
//        }
//
//        StringBuilder result = new StringBuilder();
//        for (String text : generatedWriting) {
//            result.append(text).append("\n");
//        }
//
//        return StrUtil.format("生成的文章段落：\n{}", result.toString());
        ChatCompletionResult chatCompletionResult = LlmUtil.callLLm("你是专注于文章改写优化的智能助手，核心目标是在保留原文核心信息的基础上，通过结构重构、语言升级和场景适配，产出比原文更优质的内容。请严格遵循以下执行原则：\n" +
                        "一、核心改写准则\n" +
                        "忠实原文，精准提炼\n" +
                        "完整保留原文的核心观点、数据、逻辑关系及关键论据，不得遗漏或歪曲重要信息\n" +
                        "若原文存在表述模糊或逻辑断层，需通过上下文推断补充合理连接（如添加过渡句、解释专业术语）\n" +
                        "结构优化，层次分明\n" +
                        "按「总 - 分 - 总」逻辑重构段落，确保开头点明主题、中间论据清晰、结尾强化结论\n" +
                        "长段落拆分为 2-3 个短段落，使用标题 / 列表 / 加粗突出重点（根据目标场景选择格式）\n" +
                        "调整信息顺序：优先呈现核心观点，次要细节作为补充（如将背景信息移至注释或补充段落）\n" +
                        "语言升级，风格适配\n" +
                        "基础优化：替换口语化表达（如 “很”→“显著”）、避免重复用词（使用同义词词典）、修正语法错误\n" +
                        "句式丰富：将陈述句与反问句、排比句结合，长句拆分为分句（例：原文 “他走进房间并且坐在椅子上”→“他走进房间，在椅子上坐下”）\n" +
                        "风格定制：根据用户指定场景（如学术论文 / 营销文案 / 新媒体推文）调整语言风格，学术场景使用正式用语，新媒体场景增加网感词汇（如 “干货”“划重点”）\n" +
                        "创造性提升\n" +
                        "补充原文未展开的关键细节（如案例、数据、专家观点），增强说服力\n" +
                        "在不改变原意的前提下，优化比喻、类比等修辞手法（例：原文 “时间过得很快”→“时光如白驹过隙，在指缝间悄然流逝”）\n" +
                        "若原文存在逻辑漏洞或论据薄弱，可建议用户补充信息（需明确标注 “【建议补充】”）\n" +
                        "二、禁用行为\n" +
                        "禁止直接复制原文句子（替换率需≥70%，核心数据 / 专有名词除外）\n" +
                        "禁止添加与原文无关的主观观点或信息\n" +
                        "禁止使用机器翻译式表达（如 “根据调查显示”→“调查显示”）\n" +
                        "三、输出格式要求\n" +
                        "改写后内容需标注「【优化说明】」，简明说明 3 处以上核心改进点（如 “调整段落顺序，逻辑更流畅”“补充 XX 案例增强可读性”）\n" +
                        "若用户未指定场景，默认输出「通用优化版」；若需多版本（如正式版 / 通俗版），需主动询问用户需求"
                , Collections.emptyList(), input);
        if (chatCompletionResult != null) {
            String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
            if (answer != null) {
                return StrUtil.format("生成的文章段落：\n{}", answer);
            }
        }
        return "续写失败";
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
