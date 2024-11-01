package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.LLMErrorConstants;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.SenseConvert;
import ai.openai.pojo.*;
import cn.hutool.core.text.StrFormatter;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@LLM(modelNames = {"SenseChat-Turbo","SenseChat-FunctionCall","SenseChat-5","SenseChat-128K","SenseChat-32K"})
public class SenseChatAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SenseChatAdapter.class);
    private static final String COMPLETIONS_URL = "https://api.sensenova.cn/v1/llm/chat-completions";
    private static final int HTTP_TIMEOUT = 15 * 1000;

    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        if(getSecretKey() == null || getSecretKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultModel(chatCompletionRequest);
        String token = sign(getApiKey(),  getSecretKey());
        chatCompletionRequest.setModel(getModel());
        chatCompletionRequest.setStream(false);
        LlmApiResponse completions = OpenAiApiUtil.completions(token, COMPLETIONS_URL, HTTP_TIMEOUT, chatCompletionRequest,
                SenseConvert::convert2ChatCompletionResult, SenseConvert::convertByResponse);
        if(completions.getCode() != 200) {
            logger.error("SenseChat api code {},  error {}",completions.getCode(),  completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
    }
    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        ChatCompletionResult chatCompletionResult = completions(chatCompletionRequest);
        List<ChatCompletionResult> list =  new ArrayList<>();
        list.add(chatCompletionResult);
        Iterable<ChatCompletionResult> iterable = list;
        return Observable.fromIterable(iterable);
    }


    private void setDefaultModel(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(getModel());
        }
    }
    static String sign(String ak,String sk) {
        try {
            Date expiredAt = new Date(System.currentTimeMillis() + 1800*1000);
            Date notBefore = new Date(System.currentTimeMillis() - 5*1000);
            Algorithm algo = Algorithm.HMAC256(sk);
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("alg", "HS256");
            return JWT.create()
                    .withIssuer(ak)
                    .withHeader(header)
                    .withExpiresAt(expiredAt)
                    .withNotBefore(notBefore)
                    .sign(algo);
        } catch (Exception e) {
            String msg = StrFormatter.format("{\"error\":\"{}\"}", e.getMessage());
            Integer code = LLMErrorConstants.INVALID_AUTHENTICATION_ERROR;
            logger.error("SenseChat api code {},  error {}",code, msg);
            throw new RRException(code, msg);
        }
    }
}
