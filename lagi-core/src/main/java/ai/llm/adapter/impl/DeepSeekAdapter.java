package ai.llm.adapter.impl;

import ai.annotation.LLM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LLM(modelNames = {"deepseek-chat","deepseek-r1:1.5b", "deepseek-reasoner"})
public class DeepSeekAdapter extends OpenAIStandardAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekAdapter.class);

    @Override
    public String getApiAddress() {
        if (apiAddress == null) {
            apiAddress = "https://api.deepseek.com/chat/completions";
        }
        return apiAddress;
    }
}
