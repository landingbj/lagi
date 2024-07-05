package ai.embedding;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmbeddingConstant {
    public static final String EMBEDDING_TYPE_OPENAI = "OpenAI";
    public static final String EMBEDDING_TYPE_LANDING = "Landing";
    public static final String EMBEDDING_TYPE_VICUNA = "Vicuna";
    public static final String EMBEDDING_TYPE_ERNIE = "Ernie";
    public static final String EMBEDDING_TYPE_QWEN = "Qwen";
    public static final String EMBEDDING_TYPE_ZHIPU = "Zhipu";
    public static final String EMBEDDING_TYPE_BAICHUAN = "Baichuan";
    public static final String EMBEDDING_TYPE_SENSECHAT = "SenseChat";

    public static Cache<List<String>, List<List<Float>>> getEmbeddingCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.DAYS)
                .maximumSize(10000)
                .build();
    }
}
