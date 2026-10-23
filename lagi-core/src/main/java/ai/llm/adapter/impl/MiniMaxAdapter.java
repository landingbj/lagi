package ai.llm.adapter.impl;

import ai.annotation.LLM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LLM(modelNames = {"MiniMax-M2,MiniMax-M2.1,MiniMax-M2.1-highspeed, MiniMax-M2.5, MiniMax-M2.5-highspeed"})
public class MiniMaxAdapter extends OpenAIStandardAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MiniMaxAdapter.class);

    @Override
    public String getApiAddress() {
        if (apiAddress == null) {
            apiAddress = "https://api.minimaxi.com/v1/chat/completions";
        }
        return apiAddress;
    }
}
