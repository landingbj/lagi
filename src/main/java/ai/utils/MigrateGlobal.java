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
//    public static final String SSO_LOGIN;
    public static final String VICUNA_INDEX_URL;
    public static final String ADD_DOC_INDEX_URL;
//
//    public static final String RVC_URL;
//    public static final String RVC_VOICE_CONVERT_URL;
//    public static final String RVC_TRAIN_URL;
//    public static final String UPLOAD_TRAIN_FILES_URL;
//    public static final String GET_PROGRESS_INFO_URL;
//    
//    public static final String SAM_URL;
//    public static final String IMAGE_TO_TEXT_URL;
//    public static final String RESNET_TOP_P;
//    public static final String RESNET_TOTAL_PROBS;
//    public static final String MMTRACKING_URL;
//    public static final String WOT_INFERENCE_URL;
//    public static final String MMEDITING_INFERENCE_URL;
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
	public static String SVD_URL;
	
	static {
//        Properties prop = new Properties();
//        String respath = "/migrate.properties";
//
//        try (InputStream in = ServiceInfoConfig.class.getResourceAsStream(respath);) {
//            prop.load(in);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        SSO_LOGIN = prop.getProperty("sso_login_url");
        VICUNA_INDEX_URL = "http://ec2-52-82-51-248.cn-northwest-1.compute.amazonaws.com.cn:8200";
        ADD_DOC_INDEX_URL = VICUNA_INDEX_URL + "/index/add_doc";
        LLMConfig.SEARCH_INDEX_URL = VICUNA_INDEX_URL + "/v2/index/search";
//
//        RVC_URL = prop.getProperty("rvc_url");
//        RVC_VOICE_CONVERT_URL = RVC_URL + "/voice_convert";
//        RVC_TRAIN_URL = RVC_URL + "/train";
//        UPLOAD_TRAIN_FILES_URL = RVC_URL + "/upload_train_files";
//        GET_PROGRESS_INFO_URL = RVC_URL + "/get_progress_info";
//        
//        SAM_URL = prop.getProperty("sam_url");
//        IMAGE_TO_TEXT_URL = prop.getProperty("image_to_text_url");
//        RESNET_TOP_P = prop.getProperty("resnet_top_p");
//        RESNET_TOTAL_PROBS = prop.getProperty("resnet_total_probs");
//        
//        MMTRACKING_URL = prop.getProperty("mmtracking_url");
//        WOT_INFERENCE_URL = prop.getProperty("mmtracking_url") + "/mot/inference";
//        
        FILE_PROCESS_URL  = "http://ec2-52-82-51-248.cn-northwest-1.compute.amazonaws.com.cn:8200";
    	EXTRACT_CONTENT_URL = FILE_PROCESS_URL + "/file/extract_content";
    	EXTRACT_CONTENT_WITHOUT_IMAGE_URL = FILE_PROCESS_URL + "/file/extract_content_without_image";
//    	
//    	ADD_DOCS_INDEX_URL = VICUNA_INDEX_URL + "/index/add_docs";
    	ADD_DOCS_CUSTOM_URL = VICUNA_INDEX_URL + "/index/add_docs_custom";
//    	UPDATE_DOC_IMAGE_URL = VICUNA_INDEX_URL + "/index/update_doc_image";
//    	DELETE_DOC_INDEX_URL = VICUNA_INDEX_URL + "/index/delete_doc";
//    	
//    	SEARCH_DOC_INDEX_URL = VICUNA_INDEX_URL + "/index/search";
//		ADD_INSTRUCTION_URL = VICUNA_INDEX_URL + "/instruction/add_instruction";
//		GET_ANSWER_BY_LCS_URL = VICUNA_INDEX_URL + "/instruction/get_answer_by_lcs";
//
//		WHISPER_URL = prop.getProperty("whisper_url");
//
		AI_SERVICE_URL = "http://ai.landingbj.com";
//
//		STABLE_DIFFUSION_URL = prop.getProperty("stable_diffusion_url");
//		
//		ESRGAN_URL = prop.getProperty("esrgan_url");
//		SVD_URL = prop.getProperty("svd_url");
//		
//		MMEDITING_INFERENCE_URL = prop.getProperty("mmediting_url") + "/mmediting/inference";
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
