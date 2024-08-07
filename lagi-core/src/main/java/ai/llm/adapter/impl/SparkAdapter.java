package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.utils.ObservableList;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.utils.CacheManager;
import ai.openai.pojo.*;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.listener.SparkBaseListener;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkResponse;
import io.github.briqt.spark4j.model.response.SparkResponseFunctionCall;
import io.github.briqt.spark4j.model.response.SparkResponseUsage;
import io.reactivex.Observable;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@LLM(modelNames = {"v1.1", "v2.1", "v3.1", "v3.5"})
public class SparkAdapter extends ModelService implements ILlmAdapter {

    @Override
    public boolean verify() {
        if(getAppId() == null || getAppId().startsWith("you")) {
            return false;
        }
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
        SparkRequest sparkRequest = convertRequest(chatCompletionRequest);
        SparkSyncChatResponse chatResponse = null;
        try {
            SparkClient sparkClient = new SparkClient();
            sparkClient.appid = getAppId();
            sparkClient.apiSecret = getSecretKey();
            sparkClient.apiKey = getApiKey();
            chatResponse = sparkClient.chatSync(sparkRequest);
        } catch (SparkException e) {
            e.printStackTrace();
        }
        if (chatResponse == null) {
            return null;
        }
        return convertResponse(chatResponse);
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        SparkRequest sparkRequest = convertRequest(chatCompletionRequest);
        ObservableList<ChatCompletionResult> observableList = new ObservableList<>();
        Function<SparkResponse, ChatCompletionResult> func = this::convertStreamResponse;
        SparkClient sparkClient = new SparkClient();
        sparkClient.appid = getAppId();
        sparkClient.apiSecret = getSecretKey();
        sparkClient.apiKey = getApiKey();
        sparkClient.chatStream(sparkRequest, new SparkCustomListener(observableList, func, this));
        Iterable<ChatCompletionResult> iterable = observableList.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }

    private SparkRequest convertRequest(ChatCompletionRequest request) {
        List<SparkMessage> messages = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_USER)) {
                messages.add(SparkMessage.userContent(chatMessage.getContent()));
            } else if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_ASSISTANT)) {
                messages.add(SparkMessage.assistantContent(chatMessage.getContent()));
            } else if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                messages.add(SparkMessage.systemContent(chatMessage.getContent()));
            }
        }
        String model = Optional.ofNullable(request.getModel()).orElse(getModel());
        SparkApiVersion apiVersion;
        if (model.equals(SparkApiVersion.V1_5.getVersion())) {
            apiVersion = SparkApiVersion.V1_5;
        } else if (model.equals(SparkApiVersion.V2_0.getVersion())) {
            apiVersion = SparkApiVersion.V2_0;
        } else if (model.equals(SparkApiVersion.V3_0.getVersion())) {
            apiVersion = SparkApiVersion.V3_0;
        } else if (model.equals(SparkApiVersion.V3_5.getVersion())) {
            apiVersion = SparkApiVersion.V3_5;
        } else {
            throw new RuntimeException("Unsupported model version: " + model);
        }

        return SparkRequest.builder()
                .messages(messages)
                .maxTokens(request.getMax_tokens())
                .temperature(request.getTemperature())
                .apiVersion(apiVersion)
                .build();
    }

    private ChatCompletionResult convertResponse(SparkSyncChatResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getContent());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason("stop");
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        Usage usage = new Usage();
        usage.setCompletion_tokens(response.getTextUsage().getCompletionTokens());
        usage.setPrompt_tokens(response.getTextUsage().getPromptTokens());
        usage.setTotal_tokens(response.getTextUsage().getTotalTokens());
        result.setUsage(usage);
        return result;
    }

    private ChatCompletionResult convertStreamResponse(SparkResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getPayload().getChoices().getText().get(0).getContent());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        if (response.getPayload().getChoices().getStatus() == 2) {
            choice.setFinish_reason("stop");
        }
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        SparkResponseUsage sparkUsage = response.getPayload().getUsage();
        if (sparkUsage != null) {
            Usage usage = new Usage();
            usage.setCompletion_tokens(sparkUsage.getText().getCompletionTokens());
            usage.setPrompt_tokens(sparkUsage.getText().getPromptTokens());
            usage.setTotal_tokens(sparkUsage.getText().getTotalTokens());
            result.setUsage(usage);
        }
        return result;
    }

    private static class SparkCustomListener extends SparkBaseListener {
        private static final Logger logger = LoggerFactory.getLogger(SparkCustomListener.class);
        public ObjectMapper objectMapper = new ObjectMapper();
        private SparkAdapter sparkAdapter;

        private final ObservableList<ChatCompletionResult> observableList;
        private final Function<SparkResponse, ChatCompletionResult> func;

        public SparkCustomListener(ObservableList<ChatCompletionResult> observableList, Function<SparkResponse, ChatCompletionResult> func, SparkAdapter sparkAdapter) {
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            this.observableList = observableList;
            this.func = func;
            this.sparkAdapter = sparkAdapter;
        }

        public void onMessage(String content, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
            this.observableList.add(func.apply(sparkResponse));
            if (2 == status) {
                this.observableList.onComplete();
            }
        }

        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
            logger.error("讯飞星火api发生异常：", t);
//            this.observableList.add(null);
//            sparkAdapter.setPriority(0);
            ModelService modelService = (ModelService) sparkAdapter;
            CacheManager.put(modelService.getModel(), Boolean.FALSE);
            webSocket.close(1000, "");
            this.observableList.onComplete();
            ResponseBody body = response.body();
            System.out.println(body.toString());
        }

        public void onFunctionCall(SparkResponseFunctionCall functionCall, SparkResponseUsage usage, Integer status, SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
        }
    }
}
