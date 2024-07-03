package ai.intent;

import ai.intent.pojo.IntentResult;
import ai.openai.pojo.ChatCompletionRequest;

public interface IntentService {

    IntentResult detectIntent(ChatCompletionRequest chatCompletionRequest);

}
