package ai.prompt;

import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;

public class SimplePromptFactory {

    private static final String PROMPT_TEMPLATE = "your prompt here %s";

    //方法：将给定的用户请求中message字段的内容拿出，然后将PROMPT_TEMPLATE中的"your prompt here"替换为用户请求中的message字段内容
    public ChatCompletionRequest createPrompt(ChatCompletionRequest request) {
        // 获取request.getMessages()最后一个元素
        ChatMessage message = request.getMessages().get(request.getMessages().size() - 1);
        // 获取message的text字段并替换成PROMPT_TEMPLATE中的"your prompt here"
        message.setContent(String.format(PROMPT_TEMPLATE, message.getContent()));
        return request;
    }

}
