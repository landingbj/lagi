package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.utils.PriorityLock;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import cn.hutool.core.bean.BeanUtil;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@LLM(modelNames = {"qwen-turbo","qwen-plus","qwen-max","qwen-max-1201","qwen-max-longcontext", "DeepSeek-R1-Distill-Qwen-32B"})
public class ProxyLlmAdapter extends ModelService implements ILlmAdapter {

    private final ILlmAdapter llmAdapter;


    private PriorityLock priorityLock = null;

    public ProxyLlmAdapter(ILlmAdapter llmAdapter) {
        this.llmAdapter = llmAdapter;
        if(llmAdapter instanceof ModelService) {
            ModelService modelService = (ModelService) llmAdapter;
            BeanUtil.copyProperties(modelService, this);
            if(modelService.getConcurrency() != null) {
                this.priorityLock = new PriorityLock(modelService.getConcurrency());
            }
        }
    }



    @Override
    public ChatCompletionResult completions(ChatCompletionRequest request) {
        if(!(request instanceof EnhanceChatCompletionRequest)) {
            return llmAdapter.completions(request);
        }
        if(this.priorityLock == null) {
            return llmAdapter.completions(request);
        }
        Integer priority = ((EnhanceChatCompletionRequest) request).getPriority();
        if(priority == null) {
            return llmAdapter.completions(request);
        }
        try {
//            log.info("locking priority {}", priority);
            this.priorityLock.lock(priority);
//            log.info("get lock priority {}", priority);
            ((EnhanceChatCompletionRequest) request).setPriority(null);
            ChatCompletionResult completions = llmAdapter.completions(request);
            return completions;
        } finally {
//            log.info("Unlocking priority {}", priority);
            this.priorityLock.unlock(priority);
        }
    }




    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        if(!(chatCompletionRequest instanceof EnhanceChatCompletionRequest)) {
            return llmAdapter.streamCompletions(chatCompletionRequest);
        }
        if(this.priorityLock == null) {
            return llmAdapter.streamCompletions(chatCompletionRequest);
        }
        Integer priority = ((EnhanceChatCompletionRequest) chatCompletionRequest).getPriority();
        if(priority == null) {
            return llmAdapter.streamCompletions(chatCompletionRequest);
        }
        Observable<ChatCompletionResult> completions = null;
        try {
//            log.info("stream locking priority {}", priority);
            this.priorityLock.lock(priority);
//            log.info("stream get lock priority {}", priority);
            ((EnhanceChatCompletionRequest) chatCompletionRequest).setPriority(null);
            completions = llmAdapter.streamCompletions(chatCompletionRequest);
            if(completions != null) {
                return completions.doFinally(() -> {
//                    log.info("stream Unlocking priority {}", priority);
                    this.priorityLock.unlock(priority);
                });
            } else {
                return null;
            }
        } finally {
            if(completions == null) {
                this.priorityLock.unlock(priority);
            }
        }
    }
}
