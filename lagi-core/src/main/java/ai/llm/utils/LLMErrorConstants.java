package ai.llm.utils;



public class LLMErrorConstants {

    /**
     * 请求参数不合法
     */
    public static final Integer INVALID_REQUEST_ERROR = 600;

    /**
     * 授权错误
     */
    public static final Integer INVALID_AUTHENTICATION_ERROR = 601;
    /**
     * 权限被拒绝
     */
    public static final Integer PERMISSION_DENIED_ERROR = 602;

    /**
     * 资源不存在
     */
    public static final Integer RESOURCE_NOT_FOUND_ERROR = 603;

    /**
     * 访问频率限制
     */
    public static final Integer RATE_LIMIT_REACHED_ERROR = 604;

    /**
     * 模型内部错误
     */
    public static final Integer SERVER_ERROR = 605;

    /**
     *  其他错误
     */
    public static final Integer OTHER_ERROR = 606;

    /**
     *  超时
     */
    public static final Integer TIME_OUT = 607;

    /**
     *  没有可用的模型
     */
    public static final Integer NO_AVAILABLE_MODEL = 608;
}
