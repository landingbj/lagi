package ai.common.pojo;

import ai.openai.pojo.ChatCompletionRequest;

public class ChatSession {
    private ChatResponseWithContext chatResponseWithContext;
    private ChatCompletionRequest chatCompletionRequest;

    public ChatResponseWithContext getChatResponseWithContext() {
        return chatResponseWithContext;
    }

    public void setChatResponseWithContext(ChatResponseWithContext chatResponseWithContext) {
        this.chatResponseWithContext = chatResponseWithContext;
    }

    public ChatCompletionRequest getChatCompletionRequest() {
        return chatCompletionRequest;
    }

    public void setChatCompletionRequest(ChatCompletionRequest chatCompletionRequest) {
        this.chatCompletionRequest = chatCompletionRequest;
    }
}
