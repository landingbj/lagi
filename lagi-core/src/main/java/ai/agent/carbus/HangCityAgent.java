package ai.agent.carbus;

import ai.agent.carbus.pojo.Request;
import ai.config.pojo.AgentConfig;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;

public abstract class HangCityAgent {

    protected AgentConfig config;

    public HangCityAgent(AgentConfig config) {
        this.config = config;
    }

    protected abstract Observable<ChatCompletionResult> chat(Request request);

}
