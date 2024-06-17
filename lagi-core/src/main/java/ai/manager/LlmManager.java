package ai.manager;

import ai.llm.adapter.ILlmAdapter;

public class LlmManager extends AIManager<ILlmAdapter>{

    private static final LlmManager  INSTANCE = new LlmManager();

    private LlmManager() {

    }

    public static LlmManager getInstance(){
        return INSTANCE;
    }
}
