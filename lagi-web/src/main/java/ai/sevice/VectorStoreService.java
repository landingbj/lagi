        package ai.sevice;

import ai.bigdata.BigdataService;
import ai.bigdata.pojo.TextIndexData;
import ai.common.pojo.FileChunkResponse;
import ai.common.pojo.FileInfo;
import ai.common.pojo.IndexSearchData;
import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.pojo.IntentResult;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.servlet.dto.UserRagConfig;
import ai.utils.LagiGlobal;
import ai.utils.StoppingWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorStore;
import ai.vector.loader.DefaultDocumentLoader;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.pojo.SplitConfig;
import ai.vector.loader.util.DocQaExtractor;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class VectorStoreService {
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);
    private final VectorStore vectorStore;
    private final BigdataService bigdataService;
    private final IntentService intentService;
    private final UserRagConfigService userRagConfigService;
    private final Map<String, DocumentLoader> loaderMap;
    private final ExecutorService executor;

    public VectorStoreService(VectorStore vectorStore, BigdataService bigdataService,
                              IntentService intentService, UserRagConfigService userRagConfigService) {
        this.vectorStore = vectorStore;
        this.bigdataService = bigdataService;
        this.intentService = intentService;
        this.userRagConfigService = userRagConfigService;
        this.loaderMap = initLoaderMap();
        this.executor = Executors.newFixedThreadPool(4); // 初始化线程池
    }

    // 初始化 loaderMap，映射文件后缀到 DocumentLoader
    private Map<String, DocumentLoader> initLoaderMap() {
        Map<String, DocumentLoader> map = new HashMap<>();
        // 假设存在 DefaultDocumentLoader 实现类，需根据实际替换
        map.put("docx", new DefaultDocumentLoader());
        map.put("doc", new DefaultDocumentLoader());
        map.put("txt", new DefaultDocumentLoader());
        map.put("pdf", new DefaultDocumentLoader());
        map.put("common", new DefaultDocumentLoader());
        return map;
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        String userId = (String) metadatas.get("userId");
        UserRagConfig config = userRagConfigService.getConfig(userId, category);

        Integer wenbenType = config.getWenbenChunkSize();
        Integer biaogeType = config.getBiaogeChunkSize();
        Integer tuwenType = config.getTuwenChunkSize();

        String suffix = file.getName().toLowerCase().split("\\.")[1];
        DocumentLoader documentLoader = loaderMap.getOrDefault(suffix, loaderMap.get("common"));
        List<List<FileChunkResponse.Document>> docs = documentLoader.load(
                file.getPath(),
                new SplitConfig(wenbenType, tuwenType, biaogeType, category, metadatas)
        );

        String fileName = file.getName();
        if (fileName.endsWith(".docx") || fileName.endsWith(".doc") ||
                fileName.endsWith(".txt") || fileName.endsWith(".pdf")) {
            if (config.isEnableFulltext()) {
                docs = DocQaExtractor.parseText(docs);
            }
        }

        for (List<FileChunkResponse.Document> docList : docs) {
            List<FileInfo> fileList = getFileInfoList(metadatas, docList);
            upsertFileVectors(fileList, category);
        }
    }

    // 将 metadatas 和 docList 转换为 FileInfo 列表
    private List<FileInfo> getFileInfoList(Map<String, Object> metadatas, List<FileChunkResponse.Document> docList) {
        List<FileInfo> fileList = new ArrayList<>();
        for (FileChunkResponse.Document doc : docList) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setEmbedding_id((String) metadatas.get("file_id"));
            fileInfo.setText(doc.getText());
            fileInfo.setMetadatas(metadatas);
            fileList.add(fileInfo);
        }
        return fileList;
    }

    // 插入 FileInfo 列表到向量存储
    private void upsertFileVectors(List<FileInfo> fileList, String category) {
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            UpsertRecord record = new UpsertRecord();
            record.setId(fileInfo.getEmbedding_id());
            record.setDocument(fileInfo.getText());
            record.setMetadata(fileInfo.getMetadatas());
            upsertRecords.add(record);
        }
        vectorStore.upsert(upsertRecords, category);
    }

    public void upsert(List<UpsertRecord> upsertRecords, String category) {
        String userId = (String) upsertRecords.get(0).getMetadata().get("userId");
        UserRagConfig config = userRagConfigService.getConfig(userId, category);

        for (UpsertRecord upsertRecord : upsertRecords) {
            TextIndexData data = new TextIndexData();
            data.setId(upsertRecord.getId());
            data.setText(upsertRecord.getDocument());
            data.setCategory(category);

            if (config.isEnableFulltext()) {
                bigdataService.upsert(data);
            }
        }
        this.vectorStore.upsert(upsertRecords, category);
    }

    public List<IndexSearchData> searchByContext(ChatCompletionRequest request, UserRagConfig config) {
        List<ChatMessage> messages = request.getMessages();
        IntentResult intentResult = intentService.detectIntent(request);

        if (intentResult.getIndexSearchDataList() != null) {
            return intentResult.getIndexSearchDataList();
        }

        String question = determineQuestion(messages, intentResult, request);
        return search(question, request.getCategory(), config);
    }

    public List<IndexSearchData> search(String question, String category, UserRagConfig config) {
        Map<String, String> where = new HashMap<>();
        category = (String) ObjectUtils.defaultIfNull(category, "default");

        List<IndexSearchData> indexSearchDataList = search(
                question,
                config.getSimilarityTopK(),
                config.getSimilarityCutoff(),
                where,
                category
        );

        if (config.isEnableFulltext()) {
            Set<String> esIds = bigdataService.getIds(question, category);
            if (esIds != null && !esIds.isEmpty()) {
                Set<String> indexIds = indexSearchDataList.stream()
                        .map(IndexSearchData::getId)
                        .collect(Collectors.toSet());
                indexIds.retainAll(esIds);
                indexSearchDataList = indexSearchDataList.stream()
                        .filter(indexSearchData -> indexIds.contains(indexSearchData.getId()))
                        .collect(Collectors.toList());
            }
        }

        String finalCategory = category;
        List<Future<IndexSearchData>> futureResultList = indexSearchDataList.stream()
                .map(indexSearchData -> executor.submit(() -> extendIndexSearchData(indexSearchData, finalCategory)))
                .collect(Collectors.toList());

        return futureResultList.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        logger.error("Error processing future result", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 搜索方法，接受 5 个参数
    private List<IndexSearchData> search(String question, int similarityTopK, double similarityCutoff,
                                         Map<String, String> where, String category) {
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setText(question);
        queryCondition.setN(similarityTopK);
        queryCondition.setSimilarityCutoff(similarityCutoff);
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = vectorStore.query(queryCondition, category);
        // 转换为 IndexSearchData
        return indexRecords.stream()
                .map(record -> {
                    IndexSearchData data = new IndexSearchData();
                    data.setId(record.getId());
                    data.setContent(record.getDocument());
                    // 假设 IndexSearchData 有 setContent 方法，需根据实际定义
                    return data;
                })
                .collect(Collectors.toList());
    }

    // 扩展 IndexSearchData，占位实现
    private IndexSearchData extendIndexSearchData(IndexSearchData indexSearchData, String category) {
        // 占位实现，可根据需求添加 category 相关元数据
        indexSearchData.setCategory(category); // 假设 IndexSearchData 有 setCategory 方法
        return indexSearchData;
    }

    private String determineQuestion(List<ChatMessage> messages, IntentResult intentResult, ChatCompletionRequest request) {
        String question = null;
        if (intentResult.getStatus() != null &&
                intentResult.getStatus().equals(IntentStatusEnum.CONTINUE.getName())) {
            if (intentResult.getContinuedIndex() != null) {
                ChatMessage chatMessage = messages.get(intentResult.getContinuedIndex());
                String content = chatMessage.getContent();
                String[] split = content.split("[\\s,，。！!?？]");
                String source = Arrays.stream(split)
                        .filter(StoppingWordUtil::containsStoppingWorlds)
                        .findAny()
                        .orElse("");
                if (source.isEmpty()) {
                    String.format("source = content %s", source);
                }
                if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                    source = "";
                }
                question = source + ChatCompletionUtil.getLastMessage(request);
            } else {
                List<ChatMessage> userMessages = messages.stream()
                        .filter(m -> m.getRole().equals("user"))
                        .collect(Collectors.toList());
                if (userMessages.size() > 1) {
                    question = userMessages.get(userMessages.size() - 2).getContent().trim();
                }
            }
        }
        if (question == null) {
            question = ChatCompletionUtil.getLastMessage(request);
        }
        return question;
    }
}
