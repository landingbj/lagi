package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.utils.MappingIterable;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.LlmApiResponse;
import ai.llm.utils.OpenAiApiUtil;
import ai.llm.utils.convert.QwenConvert;
import ai.openai.pojo.*;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolCallBase;
import com.alibaba.dashscope.tools.ToolFunction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@LLM(modelNames = {"qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-1201", "qwen-max-longcontext"})
public class QwenAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(QwenAdapter.class);
    private final ObjectMapper mapper;
    private static final int HTTP_TIMEOUT = 30 * 1000;

    public QwenAdapter() {
        this.mapper = new ObjectMapper();
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String getApiAddress() {
        return "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        setDefaultField(chatCompletionRequest);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + getApiKey());

        String json;
        try {
            json = mapper.writeValueAsString(chatCompletionRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LlmApiResponse completions = OpenAiApiUtil.completions(getApiKey(), getApiAddress(), HTTP_TIMEOUT, json,
                QwenConvert::convert2ChatCompletionResult, QwenConvert::convertByResponse,
                headers);
        if (completions.getCode() != 200) {
            logger.error("open ai  api error {}", completions.getMsg());
            throw new RRException(completions.getCode(), completions.getMsg());
        }
        return completions.getData();
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
            List<ToolCall> toolCalls = chatMessage.getTool_calls();
            List<ToolCallBase> collect = null;
            if (toolCalls != null) {
                collect = toolCalls.stream().map(toolCall -> {
                    com.alibaba.dashscope.tools.ToolCallFunction build = com.alibaba.dashscope.tools.ToolCallFunction.builder().build();
                    BeanUtil.copyProperties(toolCall, build);
                    return build;
                }).collect(Collectors.toList());
            }
            Message msg = Message.builder()
                    .role(chatMessage.getRole())
                    .content(chatMessage.getContent())
                    .toolCallId(chatMessage.getTool_call_id())
                    .toolCalls(collect)
                    .build();
            messages.add(msg);
        }

        boolean stream = Optional.ofNullable(request.getStream()).orElse(false);
        String model = Optional.ofNullable(request.getModel()).orElse(getModel());

        int maxTokens = request.getMax_tokens();
        if (request.getMax_tokens() >= 2000) {
            maxTokens = 2000;
        }

        List<Tool> tools = request.getTools();
        List<ToolBase> toolFunctions = null;
        if (tools != null) {
            toolFunctions = tools.stream().map(tool -> {
                String json = new Gson().toJson(tool);
                return new Gson().fromJson(json, ToolFunction.class);
            }).collect(Collectors.toList());
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
                .tools(toolFunctions)
                .build();
    }

    private ChatCompletionResult convertResponse(GenerationResult response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(response.getRequestId());
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        Message message = response.getOutput().getChoices().get(0).getMessage();
        chatMessage.setContent(message.getContent());
        chatMessage.setRole("assistant");
        List<ToolCallBase> toolCalls = message.getToolCalls();
        List<ToolCall> toolCallList = new ArrayList<>();
        if (toolCalls != null) {
            toolCallList = toolCalls.stream().map(toolCall -> {
                String json = new Gson().toJson(toolCall);
                return new Gson().fromJson(json, ToolCall.class);
            }).collect(Collectors.toList());
        }
        chatMessage.setTool_calls(toolCallList);
        choice.setMessage(chatMessage);
        choice.setFinish_reason(response.getOutput().getFinishReason());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        return result;
    }


}
