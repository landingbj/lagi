package ai.llm.utils.convert;

import ai.llm.utils.LLMErrorConstants;

public class SparkConvert {

    public static Integer convert2ErrorCode(Integer errorCode) {
        https://www.xfyun.cn/doc/spark/%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E.html
//        0	成功
//        10000	升级为ws出现错误
//        10001	通过ws读取用户的消息出错
//        10002	通过ws向用户发送消息 错
//        10003	用户的消息格式有错误
//        10004	用户数据的schema错误
//        10005	用户参数值有错误
//        10006	用户并发错误：当前用户已连接，同一用户不能多处同时连接。
//        10007	用户流量受限：服务正在处理用户当前的问题，需等待处理完成后再发送新的请求。（必须要等大模型完全回复之后，才能发送下一个问题）
//        10008	服务容量不足，联系工作人员
//        10009	和引擎建立连接失败
//        10010	接收引擎数据的错误
//        10011	发送数据给引擎的错误
//        10012	引擎内部错误
//        10013	输入内容审核不通过，涉嫌违规，请重新调整输入内容
//        10014	输出内容涉及敏感信息，审核不通过，后续结果无法展示给用户
//        10015	appid在黑名单中
//        10016	appid授权类的错误。比如：未开通此功能，未开通对应版本，token不足，并发超过授权 等等
//        10017	清除历史失败
//        10019	表示本次会话内容有涉及违规信息的倾向；建议开发者收到此错误码后给用户一个输入涉及违规的提示
//        10110	服务忙，请稍后再试
//        10163	请求引擎的参数异常 引擎的schema 检查不通过
//        10222	引擎网络异常
//        10907	token数量超过上限。对话历史+问题的字数太多，需要精简输入
//        11200	授权错误：该appId没有相关功能的授权 或者 业务量超过限制
//        11201	授权错误：日流控超限。超过当日最大访问量的限制
//        11202	授权错误：秒级流控超限。秒级并发超过授权路数限制
//        11203	授权错误：并发流控超限。并发路数超过授权路数限制
        if(errorCode == 10001 || errorCode == 10002 || errorCode == 10003 || errorCode == 10004
                || errorCode == 10005
                || errorCode == 10163
                || errorCode == 10907
        ) {
            return LLMErrorConstants.INVALID_REQUEST_ERROR;
        }
        if(errorCode == 10006 || errorCode == 10008 || errorCode == 11200) {
            return LLMErrorConstants.PERMISSION_DENIED_ERROR;
        }
        if( errorCode == 10007 || errorCode == 11202) {
            return LLMErrorConstants.RATE_LIMIT_REACHED_ERROR;
        }
        if( errorCode == 11201  || errorCode == 11203) {
            return LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
        }
        return LLMErrorConstants.OTHER_ERROR;
    }

}
