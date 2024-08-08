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
    public static final int TOTAL_THREAD_COUNT = 1;
    public static final int PRODUCER_LIMIT = 1;
    public static final int POOL_CACHE_SIZE = 10000;
    public static final int COMPLETION_CACHE_SIZE = 10000;
    public static final int RAW_ANSWER_CACHE_SIZE = 10000;

    public static final String DIVERSIFY_PROMPT = "以下是用户当前的问题，请预测该用户接下来可能会问什么其它问题？只返回问题内容本身：\n";

    public static String LOCATE_ALGORITHM = "hash";
    public static boolean MEDUSA_ENABLE = false;
    public static String MEDUSA_CATEGORY = "medusa";
    public static final int QA_SIMILARITY_TOP_K = 10;
    public static final double QA_SIMILARITY_CUTOFF = 0.1;

    public static final int WRITE_CACHE_THREADS = 5;
    public static final int SUBSTRING_THRESHOLD = 2;
    public static final int START_CORE_THRESHOLD = 3;
    public static final int ANSWER_CORE_THRESHOLD = 2;
    public static final double LCS_RATIO_QUESTION = 0.5;
    public static final int TRUNCATE_LENGTH = 20;
    public static final double LCS_RATIO_PROMPT_INPUT = 0.8;



    @Getter
    private static Boolean enableLlmDiver = true;
    @Getter
    private static Boolean enableTreeDiver = true;
    @Getter
    private static Boolean enableRagDiver = true;
    @Getter
    private static Boolean enablePageDiver = true;


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
        }
    }
}
