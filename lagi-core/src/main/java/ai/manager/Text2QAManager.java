package ai.manager;

import ai.llm.adapter.ILlmAdapter;

public class Text2QAManager extends AIManager<ILlmAdapter>{
    private static final Text2QAManager INSTANCE = new Text2QAManager();

    private Text2QAManager() {

    }

    public static Text2QAManager getInstance(){
        return INSTANCE;
    }
}
