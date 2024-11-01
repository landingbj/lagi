package ai.llm.utils.convert;

import ai.common.exception.RRException;
import ai.llm.utils.LLMErrorConstants;
import com.aliyun.tea.ValidateException;
import com.baidubce.qianfan.model.exception.ApiException;
import com.baidubce.qianfan.model.exception.AuthException;
import com.baidubce.qianfan.model.exception.QianfanException;
import com.baidubce.qianfan.model.exception.RequestException;


public class ErnieConvert {



   public static RRException convert2RRexception(Exception e) {
       if(e instanceof ApiException) {
           return new RRException(LLMErrorConstants.PERMISSION_DENIED_ERROR, e.getMessage());
       }
        if(e instanceof AuthException) {
            return new RRException(LLMErrorConstants.INVALID_AUTHENTICATION_ERROR, e.getMessage());
        }
        if(e instanceof RequestException) {
            return new RRException(LLMErrorConstants.INVALID_REQUEST_ERROR, e.getMessage());
        }
        if(e instanceof QianfanException) {
            return new RRException(LLMErrorConstants.SERVER_ERROR, e.getMessage());
        }
        if(e instanceof ValidateException) {
            return new RRException(LLMErrorConstants.INVALID_AUTHENTICATION_ERROR, e.getMessage());
        }
        return new RRException(LLMErrorConstants.OTHER_ERROR, e.getMessage());
   }

}
