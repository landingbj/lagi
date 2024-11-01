package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;
import ai.common.utils.MappingIterable;
import ai.llm.utils.convert.QwenConvert;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@LLM(modelNames = {"qwen-turbo","qwen-plus","qwen-max","qwen-max-1201","qwen-max-longcontext"})
public class QwenAdapter extends ModelService implements ILlmAdapter {


    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        Generation gen = new Generation();
        GenerationParam param = convertRequest(chatCompletionRequest);
        GenerationResult result;
        try {
            result = gen.call(param);
        } catch (Exception e) {
            RRException exception = QwenConvert.convert2RRexception(e);
            log.error("qwen  api code {} error {}", exception.getCode(), exception.getMsg());
            throw new RRException(exception.getCode(), exception.getMsg());
        }
        return convertResponse(result);
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        Generation gen = new Generation();
        GenerationParam param = convertRequest(chatCompletionRequest);
        Flowable<GenerationResult> result = null;
        try {
            result = gen.streamCall(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        Iterable<GenerationResult> resultIterable = result.blockingIterable();
        try {
            boolean b = resultIterable.iterator().hasNext();
        } catch (ApiException e) {
            RRException exception = QwenConvert.convert2RRexception(e);
            log.error("qwen  stream  api code {} error {}", exception.getCode(), exception.getMsg());
            throw exception;
        }
        Iterable<ChatCompletionResult> iterable = new MappingIterable<>(resultIterable, this::convertResponse);
        return Observable.fromIterable(iterable);
    }

    private GenerationParam convertRequest(ChatCompletionRequest request) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            Message msg = Message.builder()
                    .role(chatMessage.getRole())
                    .content(chatMessage.getContent())
                    .build();
            messages.add(msg);
        }

        boolean stream = Optional.ofNullable(request.getStream()).orElse(false);
        String model = Optional.ofNullable(request.getModel()).orElse(getModel());

        int maxTokens = request.getMax_tokens();
        if (request.getMax_tokens() >= 2000) {
            maxTokens = 2000;
        }

        return GenerationParam.builder()
                .apiKey(getApiKey())
                .model(model)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .maxTokens(maxTokens)
                .temperature((float) request.getTemperature())
                .enableSearch(stream)
                .incrementalOutput(stream)
                .build();
    }

    private ChatCompletionResult convertResponse(GenerationResult response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(response.getRequestId());
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getOutput().getChoices().get(0).getMessage().getContent());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason(response.getOutput().getFinishReason());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        return result;
    }

    private void setDefaultModel(ChatCompletionRequest chatCompletionRequest) {
        if(chatCompletionRequest.getModel() == null) {
            chatCompletionRequest.setModel(getModel());
        }
    }
}
