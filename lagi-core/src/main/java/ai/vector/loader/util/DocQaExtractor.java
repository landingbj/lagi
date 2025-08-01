package ai.vector.loader.util;

import ai.common.pojo.Backend;
import ai.common.pojo.FileChunkResponse;
import ai.config.ContextLoader;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.JsonExtractor;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorStoreConstant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class DocQaExtractor {
    private static final Backend text2qaBackend = ContextLoader.configuration.getFunctions().getText2qa();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static final String PROMPT_TEMPLATE = "请将长文本分割成更易于管理和处理的较小段落，以适应模型的输入限制，同时保持文本的连贯性和上下文信息。" +
            "将提供的需要拆分的内容拆分成多个问答对，以指定格式生成一个 JSON 文件，" +
            "且仅返回符合要求的 JSON 内容，不要附带其他解释或回答。\n" +
            "注意:\n" +
            "1.仅返回指定格式的json内容，不用额外回答其它任何内容。\n" +
            "2.返回的文件内容要完整。\n" +
            "3.要尽可能保留原文内容的完整性。\n" +
            "4.返回格式为:\n" +
            "[\n" +
            "    {\n" +
            "      \"instruction\": \"问题\",\n" +
            "      \"output\": \"答案\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"instruction\": \"问题\",\n" +
            "      \"output\": \"答案\",\n" +
            "    }\n" +
            "]\n" +
            "需要拆分的内容:\n%s";

    private final static CompletionsService completionService = new CompletionsService();

    /**
     * 串行处理，已弃用--
     * @param docs
     * @return
     * @throws JsonProcessingException
     */
    public static List<List<FileChunkResponse.Document>> parseText1(List<List<FileChunkResponse.Document>> docs) throws JsonProcessingException {
        if (text2qaBackend == null || !text2qaBackend.getEnable()) {
            return docs;
        }
        List<List<FileChunkResponse.Document>> result = new ArrayList<>();
        for (List<FileChunkResponse.Document> documentList : docs) {
            List<FileChunkResponse.Document> qaDocs = new ArrayList<>();
            for (int i = 0; i < documentList.size(); i++) {
                FileChunkResponse.Document document = documentList.get(i);
                String prompt = String.format(PROMPT_TEMPLATE, document.getText());
                String json = extract(prompt);
                if (json == null) {
                    throw new RuntimeException("Extracted JSON is null, please check the prompt or backend configuration.");
                }
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, String>> dataList = objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {
                });
                for (Map<String, String> map : dataList) {
                    String instruction = map.get("instruction");
                    FileChunkResponse.Document doc = new FileChunkResponse.Document();
                    doc.setText(instruction);
                    doc.setSource(VectorStoreConstant.FILE_CHUNK_SOURCE_LLM);
                    qaDocs.add(doc);
                }
                qaDocs.add(document);
            }
            result.add(qaDocs);
        }
        return result;
    }
    //并行处理
    public static List<List<FileChunkResponse.Document>> parseText(List<List<FileChunkResponse.Document>> docs) throws JsonProcessingException {
        if (text2qaBackend == null || !text2qaBackend.getEnable()) {
            return docs;
        }

        long startTimeMillis = System.currentTimeMillis();
        List<List<FileChunkResponse.Document>> result = new ArrayList<>();
        Integer count = POOL_SIZE!=0 ? POOL_SIZE : 5;
        ExecutorService executorService = Executors.newFixedThreadPool(count); // 控制线程池大小
        List<CompletableFuture<List<FileChunkResponse.Document>>> futures = new ArrayList<>();

        try {
            for (List<FileChunkResponse.Document> documentList : docs) {
                CompletableFuture<List<FileChunkResponse.Document>> future = CompletableFuture.supplyAsync(() -> {
                    List<FileChunkResponse.Document> qaDocs = new ArrayList<>();
                    List<CompletableFuture<Void>> innerFutures = new ArrayList<>();

                    for (int i = 0; i < documentList.size(); i++) {
                        FileChunkResponse.Document document = documentList.get(i);
                        innerFutures.add(CompletableFuture.runAsync(() -> {
                            String prompt = String.format(PROMPT_TEMPLATE, document.getText());
                            String json = extract(prompt);
                            if (json == null) {
                                log.error("Extracted JSON is null, please check the prompt or backend configuration.");
                                qaDocs.add(document);
                            }else {
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    List<Map<String, String>> dataList = objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
                                    for (Map<String, String> map : dataList) {
                                        String instruction = map.get("instruction");
                                        FileChunkResponse.Document doc = new FileChunkResponse.Document();
                                        doc.setText(instruction);
                                        doc.setSource(VectorStoreConstant.FILE_CHUNK_SOURCE_LLM);
                                        qaDocs.add(doc);
                                    }
                                    qaDocs.add(document);
                                } catch (JsonProcessingException e) {
//                                System.out.println(document + " JSON解析错误：" + json);
                                    log.error(document + " JSON解析错误：" + json);
                                    qaDocs.add(document);
                                }
                            }
                        }, executorService));
                    }

                    // 等待所有子任务完成
                    CompletableFuture.allOf(innerFutures.toArray(new CompletableFuture[0])).join();
                    return qaDocs;
                }, executorService);

                futures.add(future);
            }

            // 等待所有任务完成并收集结果
            for (CompletableFuture<List<FileChunkResponse.Document>> future : futures) {
                result.add(future.get());
            }

            long endTimeMillis = System.currentTimeMillis();
            long durationMillis = endTimeMillis - startTimeMillis;
            System.out.println("上传文件总耗时（毫秒）： " + durationMillis);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return docs;
        } finally {
            executorService.shutdown();  // 关闭线程池
        }
    }

    public static List<List<FileChunkResponse.Document>> parseText2(List<List<FileChunkResponse.Document>> docs) throws JsonProcessingException {
        if (text2qaBackend == null || !text2qaBackend.getEnable()) {
            return docs;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);

        List<CompletableFuture<List<FileChunkResponse.Document>>> futures = new ArrayList<>();

        for (List<FileChunkResponse.Document> documentList : docs) {
            CompletableFuture<List<FileChunkResponse.Document>> future = CompletableFuture.supplyAsync(() -> {
                List<FileChunkResponse.Document> qaDocs = new ArrayList<>();
                for (FileChunkResponse.Document document : documentList) {
                    String prompt = String.format(PROMPT_TEMPLATE, document.getText());
                    String json = extract(prompt);
                    if (json == null) {
                        log.error("Extracted JSON is null, prompt: {}", prompt);
                        qaDocs.add(document);
                        continue;
                    }
                    try {
                        List<Map<String, String>> dataList = OBJECT_MAPPER.readValue(json, new TypeReference<List<Map<String, String>>>() {});
                        for (Map<String, String> map : dataList) {
                            FileChunkResponse.Document doc = new FileChunkResponse.Document();
                            doc.setText(map.get("instruction"));
                            doc.setSource(VectorStoreConstant.FILE_CHUNK_SOURCE_LLM);
                            qaDocs.add(doc);
                        }
                        qaDocs.add(document);
                    } catch (JsonProcessingException e) {
                        log.error("JSON解析错误，原文内容：{}", document.getText(), e);
                        qaDocs.add(document);
                    }
                }
                return qaDocs;
            }, executorService);

            futures.add(future);
        }

        List<List<FileChunkResponse.Document>> result = new ArrayList<>();
        for (CompletableFuture<List<FileChunkResponse.Document>> future : futures) {
            result.add(future.join());
        }

        executorService.shutdown();

        return result;
    }

    public static String extract(String prompt) {
        String model = text2qaBackend.getModel();
        ChatCompletionRequest chatCompletionRequest = getCompletionRequest(model, prompt);
        ChatCompletionResult chatCompletionResult = completionService.completions(chatCompletionRequest);
        String content = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        return JsonExtractor.extractFirstJsonArray(content);
    }

    private static ChatCompletionRequest getCompletionRequest(String model, String prompt) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.6);
        chatCompletionRequest.setStream(false);
        chatCompletionRequest.setMax_tokens(4096);
        chatCompletionRequest.setModel(model);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage message = new ChatMessage();
        message.setRole(LagiGlobal.LLM_ROLE_USER);
        message.setContent(prompt);
        messages.add(message);
        chatCompletionRequest.setMessages(messages);
        return chatCompletionRequest;
    }
}
