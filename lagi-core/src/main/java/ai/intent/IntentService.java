package ai.intent;

import ai.intent.pojo.IntentResult;
import ai.openai.pojo.ChatMessage;

import java.util.List;

public interface IntentService {

    IntentResult detectIntent(List<ChatMessage> messages);

}
