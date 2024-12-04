package ai.manager;

import ai.llm.adapter.ILlmAdapter;

public class LlmInstructionManager extends AIManager<ILlmAdapter>{

    private static final LlmInstructionManager INSTANCE = new LlmInstructionManager();

    private LlmInstructionManager() {

    }

    public static LlmInstructionManager getInstance(){
        return INSTANCE;
    }
}
