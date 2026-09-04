package ai.llm.adapter.impl;

import ai.annotation.LLM;

/**
 * Adapter for Meta Model API. The service supports both OpenAI Chat
 * Completions and Responses protocols, which are implemented by the parent.
 */
@LLM(company = "Meta", modelNames = {
        "muse-spark-1.3",
        "muse-spark-1.2",
        "muse-spark-1.1",
        "muse-spark-1.3-contributor",
        "muse-spark-1.2-contributor"
})
public class MetaModelAdapter extends OpenAIStandardAdapter {
    private static final String DEFAULT_API_ADDRESS = "https://api.meta.ai/v1/chat/completions";

    @Override
    public String getApiAddress() {
        if (apiAddress == null) {
            apiAddress = DEFAULT_API_ADDRESS;
        }
        return apiAddress;
    }
}
