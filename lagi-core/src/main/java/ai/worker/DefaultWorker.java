package ai.worker;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.router.Routers;


public class DefaultWorker {

    private final Routers routers;

    public DefaultWorker() {
        this.routers = Routers.getInstance();
    }
    // pointed worker
    public ChatCompletionResult work(String router, ChatCompletionRequest data) {
        return routers.dispatch(router, data);
    }


}
