package ai.llm.utils;

import ai.common.pojo.IndexSearchData;
import ai.config.ContextLoader;
import ai.llm.pojo.GetRagContext;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.vector.VectorStoreService;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CompletionUtil {
    private static final Gson gson = new Gson();
    private static final VectorStoreService vectorStoreService = new VectorStoreService();


    public static final int MAX_INPUT;

    static {
        Integer contextLength = ContextLoader.configuration.getContextLength();
        MAX_INPUT = contextLength == null ? 1024*4  : contextLength;
    }

    public static ChatCompletionResult getDummyCompletion() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String currentDatetime = dateFormat.format(new Date());
        String message = currentDatetime + " " + UUID.randomUUID();
        String json = "{\"created\":1719495617,\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\"," +
                "\"content\":\"" + message + "\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":38," +
                "\"completion_tokens\":8,\"total_tokens\":46}}\n";
        return gson.fromJson(json, ChatCompletionResult.class);
    }

    public static void populateContext(ChatCompletionResult result, List<IndexSearchData> indexSearchDataList, GetRagContext context) {
        if (result != null && !result.getChoices().isEmpty()
                && indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            List<String> imageList = new ArrayList<>();

            for (IndexSearchData indexSearchData : indexSearchDataList) {
                if (indexSearchData.getImage() != null){
                       List<String> images = vectorStoreService.getImageFiles(indexSearchData);
                    if (images != null){
                          imageList.addAll(images);
                    }
                }
            }
            imageList = imageList.stream().distinct().collect(Collectors.toList());

            for (int i = 0; i < result.getChoices().size(); i++) {
                ChatMessage message = result.getChoices().get(i).getMessage();
                message.setContext(context.getContext());
                message.setFilename(new ArrayList<>(new HashSet<>(context.getFilenames())));
                message.setFilepath(new ArrayList<>(new HashSet<>(context.getFilePaths())));
                message.setImageList(imageList);
            }
        }
    }

    public static String truncate(String context) {
        return truncate(context, MAX_INPUT);
    }

    public static String truncate(String context, int maxLength) {
        if(context == null) {
            return "";
        }
        if(context.length() <= maxLength) {
            return context;
        }
        return context.substring(0, maxLength);
    }

    public static List<ChatMessage> truncateChatMessages(List<ChatMessage> chatMessages) {
        return truncateChatMessages(chatMessages, MAX_INPUT);
    }

    public static List<ChatMessage> truncateChatMessages(List<ChatMessage> chatMessages, int maxLength) {
        if(chatMessages != null && !chatMessages.isEmpty()) {
            ChatMessage systemChatMessage = null;
            if (chatMessages.get(0).getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                systemChatMessage = chatMessages.get(0);
            }
            ChatMessage lastQuestion = chatMessages.get(chatMessages.size() - 1);
            int userMaxLength = maxLength;
            if (systemChatMessage != null) {
                userMaxLength = maxLength - systemChatMessage.getContent().length();
            }
            lastQuestion.setContent(truncate(lastQuestion.getContent(), userMaxLength));
            int length = lastQuestion.getContent().length();
            int lastIndex = chatMessages.size() - 1;
            for(int i = chatMessages.size() - 2; i >= 0; i--) {
                ChatMessage chatMessage = chatMessages.get(i);
                length += chatMessage.getContent().length();
                if(length > userMaxLength) {
                    break;
                }
                if(chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_USER)) {
                    lastIndex = i;
                }
            }
            chatMessages = chatMessages.subList(lastIndex, chatMessages.size());
            if (systemChatMessage != null) {
                chatMessages.add(0, systemChatMessage);
            }
        }
        return chatMessages;
    }


    public static void main(String[] args) {
        ChatCompletionResult result = getDummyCompletion();
        System.out.println(result.getChoices().get(0).getMessage().getContent());
    }
}
