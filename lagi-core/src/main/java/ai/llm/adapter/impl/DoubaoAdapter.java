package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.llm.adapter.ILlmAdapter;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@LLM(modelNames = {"doubao-pro-32k,doubao-pro-4k"})
public class DoubaoAdapter  extends ModelService implements ILlmAdapter {


    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    private String getModelEndpoint(String model) {
        Map<String, String> collect = Arrays.stream(alias.split(",")).collect(Collectors.toMap(a -> a.split("=")[0], a -> a.split("=")[1]));
        return collect.get(model);
    }
    @Override
    public ChatCompletionResult completions(ChatCompletionRequest request) {
        ArkService service = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3/").build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包人工智能助手").build();
        ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(request.getMessages().get(0).getContent()).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest chatCompletionRequest = com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest.builder()
                .model(getModelEndpoint(model))
                .messages(messages)
                .build();
        com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult chatCompletion = service.createChatCompletion(chatCompletionRequest);

        ChatCompletionResult result = new ChatCompletionResult();
        BeanUtil.copyProperties(chatCompletion, result);
        return result;
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest request) {
        ArkService service = ArkService.builder().apiKey(apiKey).baseUrl("https://ark.cn-beijing.volces.com/api/v3/").build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包人工智能助手").build();
        ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(request.getMessages().get(0).getContent()).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest streamChatCompletionRequest = com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest.builder()
                .model(getModelEndpoint(model))
                .messages(messages)
                .build();

        Flowable<ChatCompletionChunk> flowable = service.streamChatCompletion(streamChatCompletionRequest);
        Observable<ChatCompletionResult> iterable = Observable.create(observableEmitter -> {
                try {
                    flowable.blockingForEach(chatCompletionChunk -> {
                        System.out.println(JSONUtil.toJsonStr(chatCompletionChunk));
                        if (chatCompletionChunk.getChoices().size()>0){
                            observableEmitter.onNext(convertResponse(chatCompletionChunk));
                            if (chatCompletionChunk.getChoices().get(0).getMessage().getContent().equals("")){
                                service.shutdownExecutor();
                            }
                        }
                    });
                }catch (Exception e) {
                    System.out.println(e);
                }
                observableEmitter.onComplete();
            });
        return iterable;
    }

    private ChatCompletionResult convertResponse(ChatCompletionChunk chatCompletionChunk) {
        ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
        if (chatCompletionResult!=null) {
            BeanUtil.copyProperties(chatCompletionChunk, chatCompletionResult);
        }
        return chatCompletionResult;
    }
}
