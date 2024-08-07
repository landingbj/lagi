package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.*;
import ai.utils.qa.HttpUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@LLM(modelNames = {"SenseChat-Turbo","SenseChat-FunctionCall","SenseChat-5","SenseChat-128K","SenseChat-32K"})
public class SenseChatAdapter extends ModelService implements ILlmAdapter {
    private final Gson gson = new Gson();
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
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String token = sign(getApiKey(),  getSecretKey());
        headers.put("Authorization", "Bearer " + token);
        String jsonResult = null;
        chatCompletionRequest.setModel(getModel());
        chatCompletionRequest.setStream(false);
        try {
            jsonResult = HttpUtil.httpPost(COMPLETIONS_URL, headers, gson.toJson(chatCompletionRequest), HTTP_TIMEOUT);
        } catch (Exception e) {
            logger.error("HttpUtil这里有错", e);
        }
        if (jsonResult == null) {
            return null;
        }
        Map<String,Object> map = gson.fromJson(jsonResult, Map.class);
        Map<String,Object> map1 = gson.fromJson(map.get("data").toString(), Map.class);
        Usage usage = gson.fromJson(map1.get("usage").toString(), Usage.class);
        List<Map<String,Object>> mapm = gson.fromJson(map1.get("choices").toString(), List.class);
        Map<String,Object> map2 = mapm.get(0);
        ChatMessage message = new ChatMessage();
        message.setContent(map2.get("message").toString());
        message.setRole("user");
        List<ChatCompletionChoice> choices = new ArrayList<>();
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        choice.setDelta(message);
        choice.setMessage(message);
        choice.setFinish_reason(map2.get("finish_reason").toString());
        choices.add(choice);
        ChatCompletionResult response = new ChatCompletionResult();
        response.setUsage(usage);
        response.setId(map1.get("id").toString());
        response.setChoices(choices);
        if (response == null || response.getChoices().isEmpty()) {
            return null;
        }
        return response;
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
            e.printStackTrace();
            return null;
        }
    }
}
