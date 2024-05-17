package ai.utils;

import java.io.*;
import java.util.List;

import ai.common.pojo.Backend;
import ai.config.AbstractConfiguration;
import ai.config.GlobalConfigurations;
import ai.config.pojo.AgentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import ai.common.pojo.Configuration;

public class LagiGlobal {
    private static Configuration config;

    public static String LANDING_API_KEY = "your-api-key";

    public static String AGENT_API_KEY = "your-api-key";

    public static Configuration getConfig() {
        return config;
    }

    public static void loadConfig(String configPath) {
        File configFile = new File(configPath);
        loadConfig(configFile);
    }

    public static void loadConfig(File configFile) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        loadConfig(inputStream, GlobalConfigurations.class);
    }

    public static AbstractConfiguration loadConfig(InputStream inputStream, Class<? extends AbstractConfiguration> cls) {
        ObjectMapper mapper = new YAMLMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try {
            AbstractConfiguration aConfig = mapper.readValue(inputStream, cls);
            config = aConfig.transformToConfiguration();
            setLandingApikey(config);
            setAgentApiKey(config);
            return aConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLandingApikey() {
        return LANDING_API_KEY;
    }

    private static void setLandingApikey(Configuration config) {
        List<Backend> backends = config.getLLM().getBackends();
        for (Backend backend : backends) {
            if (backend.getType().equalsIgnoreCase(LagiGlobal.LLM_TYPE_LANDING)) {
                String apiKey = backend.getApiKey();
                if (apiKey.startsWith("sk-") && apiKey.length() == 35) {
                    LANDING_API_KEY = apiKey;
                }
            }
        }
    }

    public static String getAgentApiKey() {
        return AGENT_API_KEY;
    }

    private static void setAgentApiKey(Configuration config) {
        List<AgentConfig> agents = config.getAgents();
        for (AgentConfig agent : agents) {
            if (agent.getApiKey() != null) {
                AGENT_API_KEY = agent.getApiKey();
                break;
            }
        }
    }

    public static final String LLM_ROLE_USER = "user";
    public static final String LLM_ROLE_ASSISTANT = "assistant";
    public static final String LLM_ROLE_SYSTEM = "system";

    public static final boolean IMAGE_EXTRACT_ENABLE = true;
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
    public static final String LLM_TYPE_SPARK = "Spark";
    public static final String LLM_TYPE_SENSECHAT = "SenseChat";
    public static final String AUDIO_TYPE_LANDING = "Landing";
    public static final String AUDIO_TYPE_ALIBABA = "Alibaba";

    public static final String IMAGE_TYPE_LANDING = "Landing";
    public static final String IMAGE_TYPE_ALIBABA = "Alibaba";


    public static final int ASR_STATUS_SUCCESS = 20000000;
    public static final int ASR_STATUS_FAILURE = 40000000;

    public static final int TTS_STATUS_SUCCESS = 20000000;
    public static final int TTS_STATUS_FAILURE = 40000000;
}
