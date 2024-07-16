package ai.llm.utils;

import ai.common.pojo.IndexSearchData;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.vector.VectorStoreService;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CompletionUtil {
    private static final Gson gson = new Gson();
    private static final VectorStoreService vectorStoreService = new VectorStoreService();

    public static ChatCompletionResult getDummyCompletion() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String currentDatetime = dateFormat.format(new Date());
        String message = currentDatetime + " " + UUID.randomUUID();
        String json = "{\"created\":1719495617,\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\"," +
                "\"content\":\"" + message + "\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":38," +
                "\"completion_tokens\":8,\"total_tokens\":46}}\n";
        return gson.fromJson(json, ChatCompletionResult.class);
    }

    public static void populateContext(ChatCompletionResult result, List<IndexSearchData> indexSearchDataList, String context) {
        if (result != null && !result.getChoices().isEmpty()
                && !indexSearchDataList.isEmpty()) {
            IndexSearchData indexData = indexSearchDataList.get(0);
            List<String> imageList = vectorStoreService.getImageFiles(indexData);
            for (int i = 0; i < result.getChoices().size(); i++) {
                ChatMessage message = result.getChoices().get(i).getMessage();
                message.setContext(context);
                if (!(indexData.getFilename() != null && indexData.getFilename().size() == 1
                        && indexData.getFilename().get(0).isEmpty())) {
                    message.setFilename(indexData.getFilename());
                }
                message.setFilepath(indexData.getFilepath());
                message.setImageList(imageList);
            }
        }
    }

    public static void main(String[] args) {
        ChatCompletionResult result = getDummyCompletion();
        System.out.println(result.getChoices().get(0).getMessage().getContent());
    }
}
