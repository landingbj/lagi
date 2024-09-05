package ai.llm.service;

import ai.common.ModelService;
import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.config.pojo.RAGFunction;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.GetRagContext;
import ai.llm.utils.CacheManager;
import ai.llm.utils.CompletionUtil;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.vector.VectorDbService;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


@Slf4j
public class RAGCompletionStaticProxy implements ChatCompletion{

    private final CompletionsService completionsService = new CompletionsService();
    private final VectorDbService vectorDbService = new VectorDbService();
    private final RAGFunction RAG_CONFIG = ContextLoader.configuration.getStores().getRag();

    private boolean valid(ChatCompletionRequest chatCompletionRequest) {
        if (chatCompletionRequest.getCategory() != null && Boolean.TRUE.equals(RAG_CONFIG.getEnable())) {
            return true;
        }
        return false;
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        boolean hasTruncate = false;
        GetRagContext context = null;
        List<IndexSearchData> indexSearchData = null;
        if(valid(chatCompletionRequest)) {
            indexSearchData = vectorDbService.searchByContext(chatCompletionRequest);
            if(indexSearchData != null) {
                context = getGetRagContext(chatCompletionRequest, indexSearchData);
            }
        }
        if(!hasTruncate) {
            List<ChatMessage> chatMessages = CompletionUtil.truncateChatMessages(chatCompletionRequest.getMessages());
            chatCompletionRequest.setMessages(chatMessages);
        }
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest, indexSearchData);
        if (context != null) {
            CompletionUtil.populateContext(result, indexSearchData, context.getContext());
        }
        return result;
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        boolean hasTruncate = false;
        GetRagContext context;
        List<IndexSearchData> indexSearchData;
        if(valid(chatCompletionRequest)) {
            indexSearchData = vectorDbService.searchByContext(chatCompletionRequest);
            if(indexSearchData != null) {
                context = getGetRagContext(chatCompletionRequest, indexSearchData);
            } else {
                context = null;
            }
        } else {
            indexSearchData = null;
            context = null;
        }
        if(!hasTruncate) {
            List<ChatMessage> chatMessages = CompletionUtil.truncateChatMessages(chatCompletionRequest.getMessages());
            chatCompletionRequest.setMessages(chatMessages);
        }
        ILlmAdapter ragAdapter = completionsService.getRagAdapter(chatCompletionRequest, indexSearchData);
        Observable<ChatCompletionResult> result = completionsService.streamCompletions(ragAdapter, chatCompletionRequest);
        AtomicBoolean e = new AtomicBoolean(true);
        if (context != null) {
            result = result.doOnError((error) -> {
                log.error("", error);
                if (ragAdapter instanceof ModelService) {
                    ModelService modelService = (ModelService) ragAdapter;
                    CacheManager.put(modelService.getModel(), false);
                }
                e.set(false);
            });
            result = result.compose(a -> {
                ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setContext(context.getContext());
                ChatCompletionChoice choice = new ChatCompletionChoice();
                choice.setMessage(chatMessage);
                chatCompletionResult.setChoices(Lists.newArrayList(choice));
                IndexSearchData indexData = indexSearchData.get(0);
                List<String> imageList = vectorDbService.getImageFiles(indexData);
                List<String> filePaths = context.getFilePaths().stream().distinct().collect(Collectors.toList());
                List<String> filenames = context.getFilenames().stream().distinct().collect(Collectors.toList());
                for (int i = 0; i < chatCompletionResult.getChoices().size(); i++) {
                    chatMessage.setContent("");
                    chatMessage.setContext(indexData.getText());
                    IndexSearchData indexData1 = indexSearchData.get(i);
                    if (!(indexData1.getFilename() != null && indexData1.getFilename().size() == 1
                            && indexData1.getFilename().get(0).isEmpty())) {
                        chatMessage.setFilename(filenames);
                        chatMessage.setFilepath(filePaths);
                    }
                    chatMessage.setImageList(imageList);
                }
                return a.concatWith(Observable.just(chatCompletionResult));
            });
        }
        if(e.get()) {
            return result;
        }
        return null;
    }

    private GetRagContext getGetRagContext(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchData) {
        GetRagContext context;
        context = completionsService.getRagContext(indexSearchData);
        String contextStr = CompletionUtil.truncate(context.getContext());
        context.setContext(contextStr);
        completionsService.addVectorDBContext(chatCompletionRequest, contextStr);
        ChatMessage chatMessage = chatCompletionRequest.getMessages().get(chatCompletionRequest.getMessages().size() - 1);
        chatCompletionRequest.setMessages(Lists.newArrayList(chatMessage));
        return context;
    }

    public static void main(String[] args) {
        ContextLoader.loadContext();
        RAGCompletionStaticProxy ragCompletionStaticProxy = new RAGCompletionStaticProxy();
        String SAMPLE_COMPLETION_RESULT_PATTERN = "{\n" +
                "    \"category\": \"chaoyang\",\n" +
                "    \"messages\": [\n" +
                "        {\n" +
                "            \"role\": \"user\",\n" +
                "            \"content\": \"%s\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"temperature\": 0.8,\n" +
                "    \"max_tokens\": 1024,\n" +
                "    \"stream\": true\n" +
                "}";
        String format = String.format(SAMPLE_COMPLETION_RESULT_PATTERN, "你好");
        ChatCompletionRequest request = JSONUtil.toBean(format, ChatCompletionRequest.class);
//        ChatCompletionResult completions = ragCompletionStaticProxy.completions(request);
//        System.out.println(JSONUtil.toJsonStr(completions));
        Observable<ChatCompletionResult> chatCompletionResultObservable = ragCompletionStaticProxy.streamCompletions(request);
        chatCompletionResultObservable.subscribe(result -> {
            System.out.println(JSONUtil.toJsonStr(result));
        });
    }

}
