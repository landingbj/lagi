package ai.vl.service;

import ai.manager.AIManager;
import ai.manager.VlLlmManager;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.VlChatCompletionRequest;
import ai.vl.adapter.VlAdapter;
import io.reactivex.Observable;

import java.util.List;

public class VlCompletionsService {

    private AIManager<VlAdapter> vlAdapterAIManager = null;

    public VlCompletionsService(){
        this.vlAdapterAIManager = VlLlmManager.getInstance();
    }

    public ChatCompletionResult completions(VlChatCompletionRequest request)
    {
        List<VlAdapter> adapters = vlAdapterAIManager.getAdapters();
        for (VlAdapter adapter : adapters) {
            ChatCompletionResult completions = adapter.completions(request);
            if (completions != null) {
                return completions;
            }
        }
        return null;
    }

    public Observable<ChatCompletionResult> streamCompletions(VlChatCompletionRequest request)
    {
        List<VlAdapter> adapters = vlAdapterAIManager.getAdapters();
        for (VlAdapter adapter : adapters) {
            return adapter.streamCompletions(request);
        }
        return null;
    }

    public boolean hasAvailableAdapter() {
        return !vlAdapterAIManager.getAdapters().isEmpty();
    }

}
