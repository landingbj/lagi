package ai.medusa.utils;

import ai.common.pojo.Medusa;
import ai.common.pojo.VectorStoreConfig;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.List;

public class PromptCacheConfig {
    public static final int POOL_INITIAL = 1;
    public static final int POOL_DIVERSIFIED = 2;
    public static final int POOL_CACHE_PUT = 3;
    public static final int PRODUCER_THREADS = 1;
    public static final int CONSUMER_THREADS = 1;
    public static final int TOTAL_THREAD_COUNT = 2;
    public static final int THREAD_RUN_LIMIT = 4;
    public static final int PRODUCER_LIMIT = 1;
    public static final int POOL_CACHE_SIZE = 10000;
    public static final int COMPLETION_CACHE_SIZE = 10000;
    public static final int RAW_ANSWER_CACHE_SIZE = 10000;

    public static final String DIVERSIFY_PROMPT = "以下是用户当前的问题，请预测该用户接下来可能会问什么其它问题？只返回问题内容本身：\n";
    public static final String REASON_DIVERSIFY_PROMPT = "### 任务\n" +
            "任务：以下提供一个提示词和推理模型的思考过程，请根据两者推测用户之后可能输入的问题。结果只返回问题本身，不需要提供相关描述和分析。如果有多种可能性，最多只返回5个问题，并将其以JSON结构输出。\n\n" +
            "### 提示词\n" +
            "```\n" +
            "%s\n" +
            "```\n\n" +
            "### 思考过程\n" +
            "```\n" +
            "%s\n" +
            "```\n\n" +
            "### 执行要求\n" +
            "1. 仅输出JSON格式，不要输出任何解释性文字。\n" +
            "2. 输出的问题是用户可能向大语言模型输入的。\n\n" +
            "### 输出格式\n" +
            "输出格式为JSON，结构如下：\n" +
            "```json\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    \"<问题1>\",\n" +
            "    \"<问题2>\",\n" +
            "    \"<问题3>\"\n" +
            "  ]\n" +
            "}\n" +
            "```\n" +
            "输出示例：\n" +
            "```json\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    \"小说的主角可以设定为岳飞的后人吗？\",\n" +
            "    \"能否加入一些宋朝的科技元素，比如火药在战斗中的应用？\",\n" +
            "    \"小说中是否可以出现真实的历史人物，如秦桧或岳飞？\"\n" +
            "  ]\n" +
            "}\n" +
            "```";

    public static String LOCATE_ALGORITHM = "hash";
    public static boolean MEDUSA_ENABLE = false;
    public static String MEDUSA_CATEGORY = "medusa";
    public static String MEDUSA_TREE_CATEGORY = "medusa_tree";
    public static final int QA_SIMILARITY_TOP_K = 10;
    public static double QA_SIMILARITY_CUTOFF = 0.1;

    public static final int WRITE_CACHE_THREADS = 5;
    public static final int SUBSTRING_THRESHOLD = 2;
    public static final int START_CORE_THRESHOLD = 3;
    public static final int ANSWER_CORE_THRESHOLD = 2;
    public static final double LCS_RATIO_QUESTION = 0.5;
    public static final int TRUNCATE_LENGTH = 20;
    public static double LCS_RATIO_PROMPT_INPUT = 0.8;



    @Getter
    private static Boolean enableLlmDiver = true;
    @Getter
    private static Boolean enableTreeDiver = true;
    @Getter
    private static Boolean enableRagDiver = true;
    @Getter
    private static Boolean enablePageDiver = true;
    @Getter
    private static Boolean enableReasonDiver = true;
    @Getter
    private static Long consumeDelay = 0L;
    @Getter
    private static Long preDelay = 0L;
    @Getter
    private static String reasonModel = null;


    public static void init(List<VectorStoreConfig> vectorStoreList, Medusa config) {
        if(vectorStoreList != null && !vectorStoreList.isEmpty() && config != null) {
            MEDUSA_ENABLE = config.getEnable() != null ? config.getEnable() : MEDUSA_ENABLE;
            LOCATE_ALGORITHM = StrUtil.isBlank(config.getAlgorithm()) ? LOCATE_ALGORITHM : config.getAlgorithm();
            String defaultCategory = vectorStoreList.get(0).getDefaultCategory();
            MEDUSA_CATEGORY = MEDUSA_CATEGORY + "-" + defaultCategory;
            enableLlmDiver = config.getEnableLlmDiver() != null ? config.getEnableLlmDiver() : enableLlmDiver;
            enableTreeDiver = config.getEnableTreeDiver() != null ? config.getEnableTreeDiver() : enableTreeDiver;
            enableRagDiver = config.getEnableRagDiver() != null ? config.getEnableRagDiver() : enableRagDiver;
            enablePageDiver = config.getEnablePageDiver() != null ? config.getEnablePageDiver() : enablePageDiver;
            enableReasonDiver = config.getEnablePageDiver() != null ? config.getEnablePageDiver() : enableReasonDiver;
            reasonModel = config.getReasonModel();
            consumeDelay = config.getConsumeDelay() != null ? config.getConsumeDelay() : consumeDelay;
            preDelay = config.getPreDelay() != null ? config.getPreDelay() : preDelay;
            QA_SIMILARITY_CUTOFF = config.getSimilarityCutoff() != null ? config.getSimilarityCutoff() : QA_SIMILARITY_CUTOFF;
            LCS_RATIO_PROMPT_INPUT = config.getLcsRatioPromptInput() != null ? config.getLcsRatioPromptInput() : LCS_RATIO_PROMPT_INPUT;
        }
    }
}
