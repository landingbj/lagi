package ai.llm;

import ai.common.exception.RRException;
import ai.llm.adapter.ILlmAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LLMManager {

    private static  final Map<String, ILlmAdapter> llmAdapters = new ConcurrentHashMap<>();

    public static void registerAdapter(String name, ILlmAdapter adapter) {
        ILlmAdapter iLlmAdapter = llmAdapters.putIfAbsent(name, adapter);
        if (iLlmAdapter != null) {
            throw new RRException("Adapter " + name + " already exists");
        }
    }

    public static ILlmAdapter getAdapter(String name) {
        return llmAdapters.get(name);
    }

}
