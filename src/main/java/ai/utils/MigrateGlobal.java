package ai.utils;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import ai.migrate.pojo.Configuration;
import ai.qa.LLMConfig;


public class MigrateGlobal {
    public static final String VICUNA_INDEX_URL;
    public static final String ADD_DOC_INDEX_URL;
    public static String FILE_PROCESS_URL;
    public static String EXTRACT_CONTENT_URL;
    public static String EXTRACT_CONTENT_WITHOUT_IMAGE_URL;
    public static String ADD_DOCS_INDEX_URL;
    public static String UPDATE_DOC_IMAGE_URL;
    public static String DELETE_DOC_INDEX_URL;
    public static String ADD_DOCS_CUSTOM_URL;
    public static String SEARCH_DOC_INDEX_URL;
    public static String ADD_INSTRUCTION_URL;
    public static String GET_ANSWER_BY_LCS_URL;
    public static String WHISPER_URL;
    public static String AI_SERVICE_URL;
    public static String STABLE_DIFFUSION_URL;
    public static String ESRGAN_URL;
    public static String ADD_INDEXES_URL;

    static {
        VICUNA_INDEX_URL = "http://ec2-52-82-51-248.cn-northwest-1.compute.amazonaws.com.cn:8200";
        ADD_DOC_INDEX_URL = VICUNA_INDEX_URL + "/index/add_doc";
        LLMConfig.SEARCH_INDEX_URL = VICUNA_INDEX_URL + "/v2/index/search";
        ADD_INDEXES_URL = VICUNA_INDEX_URL + "/v2/index/add_indexes";
        DELETE_DOC_INDEX_URL = VICUNA_INDEX_URL + "/v2/index/delete_indexes";

        FILE_PROCESS_URL = "http://ec2-52-82-51-248.cn-northwest-1.compute.amazonaws.com.cn:8200";
        EXTRACT_CONTENT_URL = FILE_PROCESS_URL + "/file/extract_content_with_image";
        EXTRACT_CONTENT_WITHOUT_IMAGE_URL = FILE_PROCESS_URL + "/file/extract_content_without_image";
        ADD_DOCS_CUSTOM_URL = VICUNA_INDEX_URL + "/index/add_docs_custom";
        AI_SERVICE_URL = "http://ai.landingbj.com";
    }

    public static final long DOC_FILE_SIZE_LIMIT = 5 * 1024 * 1024;
    public static final long IMAGE_FILE_SIZE_LIMIT = 10 * 1024 * 1024;
    public static final long AUDIO_FILE_SIZE_LIMIT = 2 * 1024 * 1024;
    public static final long VIDEO_FILE_SIZE_LIMIT = 50 * 1024 * 1024;

    public static final String INSTRUCTION_TYPE_IMAGE = "image";
    public static final String INSTRUCTION_TYPE_TEXT = "text";
    public static final String INSTRUCTION_TYPE_INSTRUCTION = "instruction";
    public static final String INSTRUCTION_TYPE_IMAGE_TO_TEXT = "imaget-to-text";
    public static final String INSTRUCTION_TYPE_MMTRACKING = "mmtracking";
    public static final String INSTRUCTION_TYPE_MMEDITING = "mmediting";
    public static final String INSTRUCTION_TYPE_SVD = "svd";
    public static final String INSTRUCTION_TYPE_SVD_BY_TEXT = "svd_by_text";
    public static final String INSTRUCTION_TYPE_ESRGAN = "esrgan";
    public static final String INSTRUCTION_TYPE_MULTILAN = "multilanguage";

    public static final String SYZX_DB = "syzx_copy";
    public static final String SEARCH_PHRASE = "phrase";
    public static final String DEFAULT_DB = "saas";
    public static final int _DEBUG_LEVEL = 1;

    public static void init() {
    }
}
