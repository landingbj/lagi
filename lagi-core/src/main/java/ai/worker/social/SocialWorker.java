package ai.worker.social;

import ai.agent.Agent;
import ai.agent.social.SocialAgent;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.worker.Worker;
import ai.worker.WorkerGlobal;

import java.util.ArrayList;
import java.util.List;

public abstract class SocialWorker extends Worker<Boolean, Boolean> {
    protected SocialAgent agent;

    protected CompletionsService completionsService = new CompletionsService();

    protected String getCompletionResult(String question) {
        ChatCompletionRequest chatCompletionRequest = getChatCompletionRequest(question);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        if (result.getChoices() == null || result.getChoices().isEmpty()) {
            return null;
        }
        return result.getChoices().get(0).getMessage().getContent();
    }

    private ChatCompletionRequest getChatCompletionRequest(String question) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(WorkerGlobal.TEMPERATURE);
        chatCompletionRequest.setStream(WorkerGlobal.STREAM);
        chatCompletionRequest.setMax_tokens(WorkerGlobal.MAX_TOKENS);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(WorkerGlobal.USER_ROLE);
        message.setContent(question);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    public Agent getAgent() {
        return agent;
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
