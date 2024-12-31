package ai.agent.proxy;

import ai.agent.Agent;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatCompletionRequest;

public abstract class ProxyAgent extends Agent<ChatCompletionRequest, ChatCompletionResult> {


    @Override
    public void connect() {

    }

    @Override
    public void terminate() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void send(ChatCompletionRequest request) {

    }

    @Override
    public ChatCompletionResult receive() {
        return null;
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        return null;
    }

}
