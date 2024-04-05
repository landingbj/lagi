package ai.utils;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import ai.common.pojo.Configuration;

public class LagiGlobal {
    public static Configuration config;

    static {
        try (InputStream inputStream = LagiGlobal.class.getResourceAsStream("/lagi.yml");) {
            ObjectMapper mapper = new YAMLMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            config = mapper.readValue(inputStream, Configuration.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final boolean IMAGE_EXTRACT_ENABLE = false;
    public static final String CHAT_COMPLETION_REQUEST = "ChatCompletionRequest";
    public static final String CHAT_COMPLETION_CONFIG = "ChatCompletionConfig";

    public static final String LANDING_MODEL_QA = "qa";
    public static final String LANDING_MODEL_TREE = "tree";
    public static final String LANDING_MODEL_TURING = "turing";

    public static final String LLM_TYPE_LANDING = "Landing";
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
