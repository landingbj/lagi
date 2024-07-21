package ai.worker.llm;

import ai.common.pojo.IndexSearchData;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorStoreService;
import ai.worker.audio.FlightAudio;
import ai.worker.pojo.AuditFile;
import ai.worker.pojo.AuditPrompt;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class AuditFileWorker {
    private static final List<AuditPrompt> AUDIT_PROMPTS;
    private static final Logger logger = LoggerFactory.getLogger(AuditFileWorker.class);
    private final VectorStoreService vectorStoreService = new VectorStoreService();
    private final CompletionsService completionsService = new CompletionsService();

    static {
        AUDIT_PROMPTS = readAuditPromptJson();
    }

    public List<AuditPrompt> getAuditPrompts() {
        return AUDIT_PROMPTS;
    }

    public ChatCompletionRequest getAuditPrompt(List<AuditFile> auditFileList, AuditPrompt auditPrompt) {
        List<IndexSearchData> indexSearchDataList = new ArrayList<>();
        for (AuditFile auditFile : auditFileList) {
            indexSearchDataList.addAll(getIndexDataList(auditPrompt.getSearchStr(), auditFile));
        }
        ChatCompletionRequest chatCompletionRequest = getChatCompletionRequest(auditPrompt.getPrompt());
        String context = getContext(indexSearchDataList);
        addVectorDBContext(chatCompletionRequest, context);
        return chatCompletionRequest;
    }

    private void addVectorDBContext(ChatCompletionRequest request, String context) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        String prompt = "以下是背景信息。\\n---------------------\\n%s\\n---------------------\\n根据上下文信息而非先前知识，回答这个问题:%s\\n";
        prompt = String.format(prompt, context, lastMessage);
        ChatCompletionUtil.setLastMessage(request, prompt);
    }

    private String getContext(List<IndexSearchData> indexSearchDataList) {
        StringBuilder context = new StringBuilder();
        for (IndexSearchData indexSearchData : indexSearchDataList) {
            context.append(indexSearchData.getText()).append("\n");
        }
        return context.toString();
    }

    public List<IndexSearchData> getIndexDataList(String searchStr, AuditFile auditFile) {
        List<IndexSearchData> result = new ArrayList<>();
        int similarity_top_k = 30;
        double similarity_cutoff = 1;
        int parentDepth = 5;
        int childDepth = 5;
        int maxIndexCount = 5;

//        int similarity_top_k = 30;
//        double similarity_cutoff = 1;
//        int parentDepth = 0;
//        int childDepth = 0;
//        int maxIndexCount = 1;

        Map<String, String> where = new HashMap<>();
        where.put("filename", auditFile.getMd5() + ".txt");
        String category = LagiGlobal.getDefaultCategory();
        List<IndexSearchData> indexSearchDataList = vectorStoreService.search(searchStr, similarity_top_k, similarity_cutoff, where, category);
        for (IndexSearchData indexSearchData : indexSearchDataList) {
            if (result.size() >= maxIndexCount) {
                break;
            }
            result.add(vectorStoreService.extendText(parentDepth, childDepth, indexSearchData, category));
        }
        return result;
    }

    private ChatCompletionRequest getChatCompletionRequest(String prompt) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setStream(true);
        chatCompletionRequest.setMax_tokens(2048);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }

    private static List<AuditPrompt> readAuditPromptJson() {
        String resFilePath = "/audit_prompt.json";
        String content = "{}";
        try (InputStream in = FlightAudio.class.getResourceAsStream(resFilePath);) {
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            logger.error("Failed to read audit prompt json file", e);
        }
        Type listType = new TypeToken<List<AuditPrompt>>() {
        }.getType();
        return new Gson().fromJson(content, listType);
    }
}
