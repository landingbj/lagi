package ai.intent;

import ai.intent.pojo.IntentResult;

import java.util.List;

public interface IntentService {

    IntentResult detectIntent(List<String> messages);

}
