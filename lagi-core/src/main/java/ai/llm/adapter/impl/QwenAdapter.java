package ai.llm.adapter.impl;

import ai.llm.adapter.ILlmAdapter;
import ai.common.utils.MappingIterable;
import ai.common.pojo.Backend;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QwenAdapter implements ILlmAdapter {
    private Backend backendConfig;

    public QwenAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        Generation gen = new Generation();
        GenerationParam param = convertRequest(chatCompletionRequest);
        GenerationResult result;
        try {
            result = gen.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
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
        String model = Optional.ofNullable(request.getModel()).orElse(backendConfig.getModel());

        return GenerationParam.builder()
                .apiKey(backendConfig.getApiKey())
                .model(model)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .maxTokens(request.getMax_tokens())
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
        chatMessage.setContent(response.getOutput().getText());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason(response.getOutput().getFinishReason());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        return result;
    }
}
