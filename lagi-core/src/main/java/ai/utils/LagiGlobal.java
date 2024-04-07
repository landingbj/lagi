package ai.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import ai.common.pojo.Configuration;

public class LagiGlobal {
    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    public static void loadConfig(String configPath) {
        File configFile = new File(configPath);
        loadConfig(configFile);
    }

    public static void loadConfig(File configFile) {
        ObjectMapper mapper = new YAMLMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try {
            config = mapper.readValue(configFile, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadConfig(InputStream inputStream) {
        ObjectMapper mapper = new YAMLMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try {
            config = mapper.readValue(inputStream, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    public static final String LLM_TYPE_ZHIPU = "Zhipu";
    public static final String LLM_TYPE_MOONSHOT = "Moonshot";
    public static final String LLM_TYPE_BAICHUAN = "Baichuan";

    public static final String AUDIO_TYPE_LANDING = "Landing";
    public static final String AUDIO_TYPE_ALIBABA = "Alibaba";

    public static final String IMAGE_TYPE_LANDING = "Landing";
    public static final String IMAGE_TYPE_ALIBABA = "Alibaba";


    public static final int ASR_STATUS_SUCCESS = 20000000;
    public static final int ASR_STATUS_FAILURE = 40000000;

    public static final int TTS_STATUS_SUCCESS = 20000000;
    public static final int TTS_STATUS_FAILURE = 40000000;
}
