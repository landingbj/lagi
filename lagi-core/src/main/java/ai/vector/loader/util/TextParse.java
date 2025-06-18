package ai.vector.loader.util;

import ai.common.pojo.FileChunkResponse;
import ai.common.pojo.FileInfo;
import ai.llm.service.CompletionsService;
import ai.manager.Text2QAManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.vector.VectorStoreService;
import ai.vector.pojo.UpsertRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;

public class TextParse {
    private final static CompletionsService completionService = new CompletionsService(Text2QAManager.getInstance());

    private static void upsertFileVectors(String category, Map<String, Object> metadatas, List<FileChunkResponse.Document> docs) throws IOException {
        Gson gson = new Gson();
        List<FileInfo> fileList = new ArrayList<>();
        for (FileChunkResponse.Document doc : docs) {
            FileInfo fileInfo = new FileInfo();
            String embeddingId = UUID.randomUUID().toString().replace("-", "");
            fileInfo.setEmbedding_id(embeddingId);
            fileInfo.setText(doc.getText());
            Map<String, Object> tmpMetadatas = new HashMap<>(metadatas);
            if (doc.getImages() != null) {
                tmpMetadatas.put("image", gson.toJson(doc.getImages()));
            }
            fileInfo.setMetadatas(tmpMetadatas);
            fileList.add(fileInfo);
        }
        VectorStoreService vectorStoreService = new VectorStoreService();
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            upsertRecords.add(vectorStoreService.convertToUpsertRecord(fileInfo));
        }
        for (int i = 1; i < upsertRecords.size(); i++) {
            String parentId = upsertRecords.get(i - 1).getId();
            upsertRecords.get(i).getMetadata().put("parent_id", parentId);
        }
        vectorStoreService.upsert(upsertRecords, category);
    }

    public static List<FileChunkResponse.Document> parseText(String filePath,List<FileChunkResponse.Document> doc,String category, Map<String, Object> metadatas){
        List<FileChunkResponse.Document> filtrationDocs = new ArrayList<>();
        if (filePath.endsWith(".docx")||filePath.endsWith(".doc")||filePath.endsWith(".txt")||filePath.endsWith(".pdf")){
            String msg = "请将长文本分割成更易于管理和处理的较小段落，以适应模型的输入限制，同时保持文本的连贯性和上下文信息。" +
                    "将需要拆分内容，拆分成多个问答对,以指定格式生成一个 JSON 文件，" +
                    "且仅返回符合要求的 JSON 内容，不要附带其他解释或回答。" +
                    "注意:" +
                    "1.仅返回指定格式的json内容，不用额外回答其它任何内容。" +
                    "2.返回的文件内容要完整。"+
                    "3.要尽可能保留原文内容的完整性。" +
                    "4.返回格式为:"+
                    "[\n" +
                    "    {\n" +
                    "      \"instruction\": \"问题\",\n" +
                    "      \"output\": \"答案\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"instruction\": \"问题\",\n" +
                    "      \"output\": \"答案\",\n" +
                    "    }\n" +
                    "]"+
                    "需要拆分的内容:"  ;
            //只需确定，问题在答案前面即可
            int indexToInsert = 0;
            Integer num = doc.size();
            for (int i = 0; i < num; i++) {
                FileChunkResponse.Document document = doc.get(indexToInsert);
                if (document.getText().length()>200){
                    try {
                        String output = chat(msg+document.getText(), null);
                        String json = output.replaceAll("```json|```", "").trim();
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<Map<String, String>> dataList = objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>(){});
                        for (Map<String, String> map : dataList) {
                            List<FileChunkResponse.Document> filtrationQADocs = new ArrayList<>();
                            FileChunkResponse.Document updatDocumentQ = document.clone();
                            updatDocumentQ.setText(map.get("instruction"));
                            filtrationQADocs.add(updatDocumentQ);
                            FileChunkResponse.Document updatDocumentA = document.clone();
                            updatDocumentA.setText(map.get("output"));
                            filtrationQADocs.add(updatDocumentA);
                            upsertFileVectors(category,  metadatas, filtrationQADocs);
                        }
                        indexToInsert++;
                    } catch (IOException e) {
                        e.printStackTrace();
                        filtrationDocs.add(document);
                        indexToInsert++;
                    }

                } else {
                    filtrationDocs.add(document);
                    indexToInsert++;
                }
            }

        } else {
            filtrationDocs = doc;
        }
        return filtrationDocs;
    }

    public static List<FileChunkResponse.Document> oldParseText(String filePath,List<FileChunkResponse.Document> doc){
        List<FileChunkResponse.Document> filtrationDocs = new ArrayList<>();
        if (filePath.endsWith(".docx")||filePath.endsWith(".doc")){
            String msg = "请将长文本分割成更易于管理和处理的较小段落，以适应模型的输入限制，同时保持文本的连贯性和上下文信息。" +
                    "将需要拆分内容，拆分成多个问答对,以指定格式生成一个 JSON 文件，" +
                    "且仅返回符合要求的 JSON 内容，不要附带其他解释或回答。" +
                    "注意:要尽可能保留原文内容的完整性。返回格式为:"+
                    "[\n" +
                    "    {\n" +
                    "      \"instruction\": \"问题\",\n" +
                    "      \"output\": \"答案\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"instruction\": \"问题\",\n" +
                    "      \"output\": \"答案\",\n" +
                    "    }\n" +
                    "]"+
                    "需要拆分的内容:"  ;
            //只需确定，问题在答案前面即可
            int indexToInsert = 0;
            Integer num = doc.size();
            for (int i = 0; i < num; i++) {
                FileChunkResponse.Document document = doc.get(indexToInsert);
                if (document.getText().length()>200){
                    String output = chat(msg+document.getText(), null);
                    String json = output.replaceAll("```json|```", "").trim();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        List<Map<String, String>> dataList = objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>(){});
                        List<FileChunkResponse.Document> textParseDoc  = new ArrayList<>();
                        for (Map<String, String> map : dataList) {
                            FileChunkResponse.Document updatDocumentQ = document.clone();
                            updatDocumentQ.setText(map.get("instruction"));
                            textParseDoc.add(updatDocumentQ);
                            FileChunkResponse.Document updatDocumentA = document.clone();
                            updatDocumentA.setText(map.get("output"));
                            textParseDoc.add(updatDocumentA);
                        }
                        doc.addAll(indexToInsert, textParseDoc);
                        doc.remove(textParseDoc.size()-1);
                        indexToInsert+=textParseDoc.size()-1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    filtrationDocs.add(document);
                    indexToInsert++;
                }
            }

        }
        return doc;
    }

    //智能问答的方法
    public static String chat(String msg, List<ChatMessage> messages) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(10000000);
        chatCompletionRequest.setCategory("default_text_parse");
        if (messages == null){
            messages = new ArrayList<>();
        }
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(msg);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        chatCompletionRequest.setStream(false);
        ChatCompletionResult result = completionService.completions(chatCompletionRequest);
        return result.getChoices().get(0).getMessage().getContent();
    }

}
