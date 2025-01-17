package ai.llm.adapter.impl;

import ai.annotation.LLM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LLM(modelNames = {"deepseek-chat"})
public class DeepSeekAdapter extends OpenAIStandardAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekAdapter.class);

    @Override
    public String getApiAddress() {
        return "https://api.deepseek.com/chat/completions";
    }
}
