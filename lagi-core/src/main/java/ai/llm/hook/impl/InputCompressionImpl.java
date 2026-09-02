package ai.llm.hook.impl;

import ai.annotation.Component;
import ai.annotation.ConditionalOnProperty;
import ai.annotation.Order;
import ai.common.ModelService;
import ai.intent.impl.SampleIntentServiceImpl;
import ai.llm.hook.BeforeModel;
import ai.llm.pojo.ModelContext;
import ai.llm.responses.ResponseProtocolUtil;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.openai.utils.MultiModalContentUtil;
import ai.utils.LagiGlobal;

import java.util.ArrayList;
import java.util.List;

@Order
@Component
@ConditionalOnProperty(name = "functions.chat.input_compression", havingValue = "true")
public class InputCompressionImpl implements BeforeModel {

    private final SampleIntentServiceImpl intentService = new SampleIntentServiceImpl();

    @Override
    public ChatCompletionRequest beforeModel(ModelContext context) {
//        System.out.println("enable InputCompressionImpl");
        if (context == null || context.getRequest() == null) {
            return null;
        }
        boolean responseProtocol = ResponseProtocolUtil.isResponseProtocol((ModelService) context.getAdapter());
        if(responseProtocol) {
            return context.getRequest();
        }
        ChatCompletionRequest request = context.getRequest();
        if (Boolean.TRUE.equals(request.getPreserveInputMessages())) {
            return request;
        }
        if (containsImageContent(request.getMessages())) {
            return request;
        }
        List<ChatMessage> merge = mergeConsecutiveMessages(request.getMessages());
        request.setMessages(merge);
        List<ChatMessage> newChatMessages = intentService.detectSegmentationBoundary(request);
        request.setMessages(newChatMessages);
        return request;
    }

    private boolean containsImageContent(List<ChatMessage> messages) {
        if (messages == null) {
            return false;
        }
        for (ChatMessage message : messages) {
            if (message != null && MultiModalContentUtil.hasImageContent(message.getContent())) {
                return true;
            }
        }
        return false;
    }

    private List<ChatMessage> mergeConsecutiveMessages(List<ChatMessage> messages) {
        List<ChatMessage> mergedList = new ArrayList<>();
        if (messages == null || messages.isEmpty()) {
            return mergedList;
        }

        ChatMessage current = messages.get(0);
        mergedList.add(current);

        for (int i = 1; i < messages.size(); i++) {
            ChatMessage next = messages.get(i);
            ChatMessage lastMerged = mergedList.get(mergedList.size() - 1);

            if (lastMerged.getRole().equals(next.getRole())
                    && (LagiGlobal.LLM_ROLE_USER.equals(next.getRole())
                    || LagiGlobal.LLM_ROLE_ASSISTANT.equals(next.getRole()))) {
                String mergedContent = lastMerged.getContent() + "\n" + next.getContent();
                lastMerged.setContent(mergedContent);
            } else {
                mergedList.add(next);
            }
        }
        return mergedList;
    }

}
