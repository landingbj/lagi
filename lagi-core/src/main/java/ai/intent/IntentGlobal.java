package ai.intent;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.pojo.LLmRequest;
import ai.utils.LRUCache;
import ai.utils.Pair;
import ai.worker.pojo.IntentResponse;

public class IntentGlobal {
    public static String MAPPER_INTENT_PARAM = "MAPPER_INTENT_PARAM";

    public static final LRUCache<LLmRequest, IntentResponse> INTENT_KEYWORD_CACHE = new LRUCache<>(1000);

    public static final LRUCache<String, Pair<Integer, Agent<ChatCompletionRequest, ChatCompletionResult>>>
            AGENT_LRU_CACHE = new LRUCache<>(1000);;
}
