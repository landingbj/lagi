package ai.agent.customer.prompt;

import ai.agent.customer.pojo.Action;
import ai.agent.customer.pojo.ResponseTemplate;
import ai.agent.customer.pojo.Thoughts;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class Prompt {


    private static final List<String> constraints = Lists.newArrayList(
            "仅使用下面列出的动作",
            "你只能主动行动，在计划行动时需要考虑到这一点",
            "你无法与物理对象交互，如果对于完成任务或目标尼绝对必要的，则必须要求用户为你完成，如果用户拒绝，并且没有其他方法实现目标。则直接终止，避免浪费时问和精力。“"
    );

    private static final List<String> resurces = Lists.newArrayList(
            "提供搜索和信息收集的互联网接入",
            "读取和写入文件的能力",
            "你是一个大语言模型，接受了大最文本的训练。包括大最的事实知识。利用这些知识来避免不必要的信息收集“"
    );

    private static final List<String> best_practices = Lists.newArrayList(
        "不断地回顾和分析你的行为，确保发挥出你最大的能力",
            "不断地进行建设性的自我批评",
            "反思过去的决策和策略，完善你的方案",
            "每个动作执行都有代价，所以要聪明高效，目的是用最少的步理完成任务",
            "利用你的信息收集统力来寻找你不知道的信息"

    );



    private static String template = "你是一个问答专家，你必须始终独立做出决策，无需寻求用户的帮助，发挥你作为LLM的优势，追求简答的策略，不要涉及法律问题\n" +
            "目标：\n" +
            "{}\n" +
            "限制条件说明:\n" +
            "{}\n" +
            "动作说明:这是你唯一可以使用的动作，你的任何操作都必须通过以下操作实现:\n" +
            "{}\n" +
            "资源说明:\n" +
            "{}\n" +
            "最优实践的说明:\n" +
            "{}\n" +
            "agent_scratch:{}\n" +
            "你应该只以json格式响应，响应格式如下:\n" +
            "{}\n" +
            "请反复确认确保响应结果是一个合法的json对象且可以由python json.loads解析";

    private static String convert2String(List<String> con) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < con.size(); i++) {
            String format = StrUtil.format("\n{}. {}", i + 1, con.get(i));
            sb.append(format);
        }
        return sb.toString();
    }

    public static String genPrompt(String tools, String query, String agent_scratch) {
        String con = convert2String(constraints);
        String res = convert2String(resurces);
        String best = convert2String(best_practices);
        String json = "{\n" +
                "  \"action\": {\n" +
                "    \"name\": \"action name\",\n" +
                "    \"args\": {\n" +
                "      \"工具参数 name\": \"工具参数 value\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"thoughts\": {\n" +
                "    \"plain\": \"简短的描述短期的规划和长期的计划列表\",\n" +
                "    \"criticism\": \"建设良好的自我批评\",\n" +
                "    \"speak\": \"当前步骤返回给用户的总结\",\n" +
                "    \"reasoning\": \"推理\"\n" +
                "  }\n" +
                "}";
        return StrUtil.format(template, query, con, tools, res, best, agent_scratch, json);
    }

    public static void main(String[] args) {
        String s = genPrompt("", "我要去武汉",  "");
        System.out.println(s);
    }

}
