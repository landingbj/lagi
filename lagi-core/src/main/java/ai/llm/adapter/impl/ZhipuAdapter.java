package ai.llm.adapter.impl;

import ai.common.ModelService;
import ai.common.pojo.Backend;
import ai.common.utils.MappingIterable;
import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatMessageAccumulator;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ZhipuAdapter extends ModelService implements ILlmAdapter {
    private final Backend backendConfig;
    private final ClientV4 client;

    public ZhipuAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
        client = new ClientV4.Builder(backendConfig.getApiKey()).build();
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(convertRequest(chatCompletionRequest));
        return convertResponse(invokeModelApiResp.getData());
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        ModelApiResponse sseModelApiResp = client.invokeModelApi(convertRequest(chatCompletionRequest));
        Iterable<ModelData> resultIterable = sseModelApiResp.getFlowable().blockingIterable();
        Iterable<ChatCompletionResult> iterable = new MappingIterable<>(resultIterable, this::convertResponse);
        return Observable.fromIterable(iterable);
    }

    private com.zhipu.oapi.service.v4.model.ChatCompletionRequest convertRequest(ChatCompletionRequest request) {
        List<com.zhipu.oapi.service.v4.model.ChatMessage> messages = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            com.zhipu.oapi.service.v4.model.ChatMessage msg =
                    new com.zhipu.oapi.service.v4.model.ChatMessage(chatMessage.getRole(), chatMessage.getContent());
            messages.add(msg);
        }

        boolean stream = Optional.ofNullable(request.getStream()).orElse(false);
        String model = Optional.ofNullable(request.getModel()).orElse(backendConfig.getModel());

        String invokeMethod = Constants.invokeMethod;
        if (request.getStream()) {
            invokeMethod = Constants.invokeMethodSse;
        }

        return com.zhipu.oapi.service.v4.model.ChatCompletionRequest.builder()
                .model(model)
                .maxTokens(request.getMax_tokens())
                .temperature((float) request.getTemperature())
                .stream(stream)
                .invokeMethod(invokeMethod)
                .messages(messages)
                .build();
    }

    private ChatCompletionResult convertResponse(ModelData modelData) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(modelData.getId());
        result.setCreated(modelData.getCreated());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        for (int i = 0; i < modelData.getChoices().size(); i++) {
            ChatCompletionChoice choice = new ChatCompletionChoice();
            ChatMessage chatMessage = new ChatMessage();
            if (choice.getDelta() == null) {
                com.zhipu.oapi.service.v4.model.Delta delta = modelData.getChoices().get(i).getDelta();
                chatMessage.setContent(delta.getContent());
                chatMessage.setRole(delta.getRole());
            } else {
                com.zhipu.oapi.service.v4.model.ChatMessage message = modelData.getChoices().get(i).getMessage();
                chatMessage.setContent(message.getContent().toString());
                chatMessage.setRole(message.getRole());
            }
            choice.setIndex(i);
            choice.setMessage(chatMessage);
            choices.add(choice);
        }
        result.setChoices(choices);
        return result;
    }
}
