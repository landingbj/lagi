package ai.worker.skillMap.prompt;

public class SkillMapPrompt {

    public static final String KEYWORD_PROMPT_TEMPLATE = "当用户向系统提问时，你需要首先对问题进行自然语言处理（NLP）。通过NLP技术，你的任务是理解用户的意图，识别问题中的关键词，并将问题进行分类。具体步骤如下：\n" +
            "\n" +
            "问题：\n" +
            "{}\n" +
            "\n" +
            "##字段说明:\n" +
            "keywords 类型：[string] 内容描述：和用户意图有关的关键词列表不包含时间地名人物 \n" +
            "#执行要求\n" +
            "1.严格遵循字段说明，不得进行任何推测。\n" +
            "2.当某个字段未找到能准确符合字段提取内容时，输出为\"无\"。\n" +
            "3.仅输出 JSON 格式，不要输出任何解释性文字。 例如：{\"keywords\":[]}\n" +
            "4.确保输出能被可以由python json.loads解析\n" +
            "5.确保 JSON 结构清晰，字段名称与内容准确对应。";

    public static final String KEYWORD_USER_PROMPT = "请提取一下用户的意图里的关键词";

    public static final String SCORE_PROMPT_TEMPLATE = "你是一个问答分析专家，你需要首先对问题和给出的回答进行自然语言处理（NLP）。通过NLP技术，你的任务是分析问题与回答的相关性并进行打分：\n" +
            "\n" +
            "问题：\n" +
            "{}\n" +
            "回答：\n" +
            "{}\n" +
            "\n" +
            "打分规则：\n" +
            "当回答表示无法获取相应的数据时 给出负的分数 -10分\n"+
            "当回答表示抱歉遗憾等愧疚的意思时 给出负的分数 -5 分\n" +
            "当回答表示遗憾并给出一般通用回答是： 给出负向分数  -3\n" +
            "当回答符合负向分数评分标准, 且信息完整结构完整语义明确： 给出正向分数 分数区间5-10\n" +
            "\n" +
            "##字段说明:\n" +
            "score： 问答相关性评分\n" +
            "#执行要求\n" +
            "1.严格遵循字段说明，不得进行任何推测。\n" +
            "2.当某个字段未找到能准确符合字段提取内容时，输出为\"无\"。\n" +
            "3.仅输出 JSON 格式，不要输出任何解释性文字。例如：{\"score\": 0}\n" +
            "4.确保 JSON 结构清晰，字段名称与内容准确对应。";

    public static final String SCORE_USER_PROMPT = "请给出问题和答案的相关性评分";

}
