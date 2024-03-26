package ai.utils;

import java.io.InputStream;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import ai.migrate.pojo.Configuration;

public class LagiGlobal {
    public static Configuration config;

    static {
        Yaml yaml = new Yaml(new Constructor(Configuration.class, new LoaderOptions()));
        try (InputStream inputStream = LagiGlobal.class.getResourceAsStream("/lagi.yml");) {
            config = yaml.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static final String CHAT_COMPLETION_REQUEST = "ChatCompletionRequest";
    public static final String CHAT_COMPLETION_CONFIG = "ChatCompletionConfig";

    public static final String LLM_TYPE_QA = "Landing-QA";
    public static final String LLM_TYPE_TREE = "Landing-Tree";
    public static final String LLM_TYPE_TURING = "Turing";
    public static final String LLM_TYPE_VICUNA = "Vicuna";
    public static final String LLM_TYPE_GPT = "GPT";
    public static final String LLM_TYPE_Qwen = "Qwen";
    public static final String LLM_TYPE_ERNIE = "Ernie";

    public static final String AUDIO_TYPE_LANDING = "Landing";
    public static final String AUDIO_TYPE_ALIBABA = "Alibaba";

    public static final String IMAGE_TYPE_LANDING = "Landing";
    public static final String IMAGE_TYPE_ALIBABA = "Alibaba";
    
    
    public static final int ASR_STATUS_SUCCESS = 20000000;
    public static final int ASR_STATUS_FAILURE = 40000000;
    
    public static final int TTS_STATUS_SUCCESS = 20000000;
    public static final int TTS_STATUS_FAILURE = 40000000;
}
