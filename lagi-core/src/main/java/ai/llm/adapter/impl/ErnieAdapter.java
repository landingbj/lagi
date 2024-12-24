package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.common.utils.MappingIterable;
import ai.llm.utils.convert.ErnieConvert;
import ai.openai.pojo.*;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.core.auth.Auth;
import com.baidubce.qianfan.model.chat.ChatRequest;
import com.baidubce.qianfan.model.chat.ChatResponse;
import com.baidubce.qianfan.model.chat.Function;
import com.baidubce.qianfan.model.chat.Message;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@LLM(modelNames = {"ERNIE-Speed-128K","ERNIE-Bot-turbo","ERNIE-4.0-8K","ERNIE-3.5-8K-0205","ERNIE-3.5-4K-0205", "ERNIE-3.5-8K-1222"})
public class ErnieAdapter extends ModelService implements ILlmAdapter {

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
        String secretKey = this.getSecretKey();
        String apiKey = this.getApiKey();
        try {
            Qianfan qianfan = new Qianfan(Auth.TYPE_OAUTH, apiKey, secretKey);
            ChatRequest request = convertRequest(chatCompletionRequest);
            ChatResponse response = qianfan.chatCompletion(request);
            return convertResponse(response);
        } catch (Exception e) {
            RRException exception = ErnieConvert.convert2RRexception(e);
            log.error("ERNIE api code {}  error {}",exception.getCode(), exception.getMsg());
            throw exception;
        }
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        String secretKey = this.getSecretKey();
        String apiKey = this.getApiKey();
        try {
            Qianfan qianfan = new Qianfan(Auth.TYPE_OAUTH, apiKey, secretKey);
            ChatRequest request = convertRequest(chatCompletionRequest);
            Iterator<ChatResponse> iterator = qianfan.chatCompletionStream(request);
            Iterable<ChatCompletionResult> iterable = new MappingIterable<>(() -> iterator, this::convertResponse);
            return Observable.fromIterable(iterable);
        } catch (Exception e) {
            RRException exception = ErnieConvert.convert2RRexception(e);
            log.error("ERNIE stream api code {}  error {}",exception.getCode(), exception.getMsg());
            throw exception;
        }
    }

    private ChatRequest convertRequest(ChatCompletionRequest request) {
        ChatRequest result = new ChatRequest();
        List<Message> messages = request.getMessages().stream().map(m->{
            Message message = new Message();
            message.setContent(m.getContent());
            message.setRole(m.getRole());
            return message;
        }).collect(Collectors.toList());

        String model = Optional.ofNullable(request.getModel()).orElse(getModel());
//        String finalEndpoint = new ModelEndpointRetriever(new IAMAuth(apiKey, secretKey)).getEndpoint("chat", model, null);
//        result.setEndpoint(finalEndpoint);
        result.setModel(model);
        result.setMessages(messages);
        result.setTemperature(request.getTemperature() == 0.0 ? 0.7 : request.getTemperature());
        result.setMaxOutputTokens(request.getMax_tokens());
        if(request.getTools() != null && (!request.getTools().isEmpty())) {
            List<Function> functions = request.getTools().stream().map(tool -> {
                Function function = new Function();
                BeanUtil.copyProperties(tool, function);
                return function;
            }).collect(Collectors.toList());
            result.setFunctions(functions);
        }
        return result;
    }

    private ChatCompletionResult convertResponse(ChatResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(response.getId());
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getResult());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason("stop");
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        Usage usage = new Usage();
        usage.setCompletion_tokens(response.getUsage().getCompletionTokens());
        usage.setPrompt_tokens(response.getUsage().getPromptTokens());
        usage.setTotal_tokens(response.getUsage().getTotalTokens());
        result.setUsage(usage);
        return result;
    }


}
