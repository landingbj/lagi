package ai.vector;

import ai.bigdata.BigdataService;
import ai.bigdata.pojo.TextIndexData;
import ai.common.pojo.*;
import ai.common.utils.ThreadPoolManager;
import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.impl.SampleIntentServiceImpl;
import ai.intent.pojo.IntentResult;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.manager.VectorStoreManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.StoppingWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.db.VectorSettingsDao;
import ai.vector.impl.BaseVectorStore;
import ai.vector.loader.DocumentLoader;
import ai.vector.loader.impl.*;
import ai.vector.loader.pojo.SplitConfig;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class VectorStoreService {
    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
    private final Gson gson = new Gson();
    private BaseVectorStore vectorStore;
    private final FileService fileService = new FileService();
    private static final ExecutorService executor;
    private Map<String, DocumentLoader> loaderMap = new HashMap<>();

    static {
        ThreadPoolManager.registerExecutor("vector-service", new ThreadPoolExecutor(30, 100, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                (r, executor) -> {
                    log.error(StrUtil.format("线程池队({})任务过多请求被拒绝", "vector-service"));
                }
        ));
        executor = ThreadPoolManager.getExecutor("vector-service");
    }

    private final IntentService intentService = new SampleIntentServiceImpl();
    private final BigdataService bigdataService = new BigdataService();
    private static final VectorCache vectorCache = VectorCache.getInstance();

    public VectorStoreService() {
//        if (LagiGlobal.RAG_ENABLE) {
        this.vectorStore = (BaseVectorStore) VectorStoreManager.getInstance().getAdapter();
        TxtLoader txtLoader = new TxtLoader();
        loaderMap.put("txt", txtLoader);

        ExcelLoader excelLoader = new ExcelLoader();
        loaderMap.put("xls", excelLoader);
        loaderMap.put("xlsx", excelLoader);

        CsvLoader csvLoader = new CsvLoader();
        loaderMap.put("csv", csvLoader);

        ImageLoader imageLoader = new ImageLoader();
        loaderMap.put("jpg", imageLoader);
        loaderMap.put("jpeg", imageLoader);
        loaderMap.put("webp", imageLoader);
        loaderMap.put("png", imageLoader);
        loaderMap.put("gif", imageLoader);
        loaderMap.put("bmp", imageLoader);

        DocLoader docLoader = new DocLoader();
        loaderMap.put("doc", docLoader);
        loaderMap.put("docx", docLoader);

        PptLoader pptLoader = new PptLoader();
        loaderMap.put("pptx", pptLoader);
        loaderMap.put("ppt", pptLoader);

        PdfLoader pdfLoader = new PdfLoader();
        loaderMap.put("pdf", pdfLoader);

        loaderMap.put("common", docLoader);

//        }
    }

    public VectorStoreConfig getVectorStoreConfig() {
        if (vectorStore == null) {
            return null;
        }
        return vectorStore.getConfig();
    }


    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        // todo 按页切割文件

//        List<FileChunkResponse.Document> docs = new ArrayList<>();
        List<UserRagSetting> userList = (List<UserRagSetting>) metadatas.get("settingList");
        Integer wenben_type = 512;
        Integer biaoge_type = 512;
        Integer tuwen_type = 512;
        if (userList != null) {
            for (UserRagSetting user : userList) {
                if ("wenben_type".equals(user.getFileType())) {
                    wenben_type = user.getChunkSize();
                    break;  // 找到后直接退出循环
                }
                if ("biaoge_type".equals(user.getFileType())) {
                    biaoge_type = user.getChunkSize();
                    break;  // 找到后直接退出循环
                }
                if ("tuwen_type".equals(user.getFileType())) {
                    tuwen_type = user.getChunkSize();
                    break;  // 找到后直接退出循环
                }
            }
        }

        String suffix = file.getName().toLowerCase().split("\\.")[1];
        DocumentLoader documentLoader = loaderMap.getOrDefault(suffix, loaderMap.get("common"));

        List<FileChunkResponse.Document> docs = documentLoader.load(file.getPath(), new SplitConfig(wenben_type, tuwen_type, biaoge_type, category, metadatas));
        List<FileInfo> fileList = new ArrayList<>();

        String fileName = metadatas.get("filename").toString();
        if (fileName != null) {
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex != -1) {
                fileName = fileName.substring(0, dotIndex);
            }
            FileInfo fi1 = new FileInfo();
            String e1 = UUID.randomUUID().toString().replace("-", "");
            fi1.setEmbedding_id(e1);
            fi1.setText(fileName);
            Map<String, Object> t1 = new HashMap<>(metadatas);
            t1.remove("parent_id");
            fi1.setMetadatas(t1);
            fileList.add(fi1);
        }

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
        upsertFileVectors(fileList, category);
    }

    public void upsertCustomVectors(List<UpsertRecord> upsertRecords, String category) {
        this.upsertCustomVectors(upsertRecords, category, false);
    }

    public void upsertCustomVectors(List<UpsertRecord> upsertRecords, String category, boolean isContextLinked) {
        for (UpsertRecord upsertRecord : upsertRecords) {
            String embeddingId = UUID.randomUUID().toString().replace("-", "");
            upsertRecord.setId(embeddingId);
        }
        if (isContextLinked) {
            for (int i = 1; i < upsertRecords.size(); i++) {
                String parentId = upsertRecords.get(i - 1).getId();
                upsertRecords.get(i).getMetadata().put("parent_id", parentId);
            }
        }
        this.upsert(upsertRecords, category);
    }

    private void upsertFileVectors(List<FileInfo> fileList, String category) throws IOException {
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        for (FileInfo fileInfo : fileList) {
            upsertRecords.add(convertToUpsertRecord(fileInfo));
        }
        for (int i = 1; i < upsertRecords.size(); i++) {
            String parentId = upsertRecords.get(i - 1).getId();
            upsertRecords.get(i).getMetadata().put("parent_id", parentId);
        }
        this.upsert(upsertRecords, category);
    }

    private UpsertRecord convertToUpsertRecord(FileInfo fileInfo) {
        UpsertRecord upsertRecord = new UpsertRecord();
        upsertRecord.setDocument(fileInfo.getText());
        upsertRecord.setId(fileInfo.getEmbedding_id());

        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, Object> entry : fileInfo.getMetadatas().entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue().toString();
            metadata.put(entry.getKey(), value);
        }
        upsertRecord.setMetadata(metadata);

        return upsertRecord;
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        this.upsert(upsertRecords, vectorStore.getConfig().getDefaultCategory());
    }

    public void upsert(List<UpsertRecord> upsertRecords, String category) {
        for (UpsertRecord upsertRecord : upsertRecords) {
            TextIndexData data = new TextIndexData();
            data.setId(upsertRecord.getId());
            data.setText(upsertRecord.getDocument());
            data.setCategory(category);
            bigdataService.upsert(data);
        }
        this.vectorStore.upsert(upsertRecords, category);
    }

    public List<IndexRecord> query(QueryCondition queryCondition) {
        return this.vectorStore.query(queryCondition);
    }

    public List<IndexRecord> query(QueryCondition queryCondition, String category) {
        return this.vectorStore.query(queryCondition, category);
    }

    public List<IndexRecord> fetch(List<String> ids) {
        return this.vectorStore.fetch(ids);
    }

    public List<IndexRecord> fetch(List<String> ids, String category) {
        return this.vectorStore.fetch(ids, category);
    }

    public IndexRecord fetch(String id) {
        List<String> ids = Collections.singletonList(id);
        List<IndexRecord> indexRecords = this.vectorStore.fetch(ids);
        IndexRecord result = null;
        if (indexRecords.size() == 1) {
            result = indexRecords.get(0);
        }
        return result;
    }

    public IndexRecord fetch(String id, String category) {
        List<String> ids = Collections.singletonList(id);
        List<IndexRecord> indexRecords = this.vectorStore.fetch(ids, category);
        IndexRecord result = null;
        if (indexRecords.size() == 1) {
            result = indexRecords.get(0);
        }
        return result;
    }

    public List<IndexRecord> fetch(Map<String, String> where) {
        return this.vectorStore.fetch(where);
    }

    public List<IndexRecord> fetch(Map<String, String> where, String category) {
        return this.vectorStore.fetch(where, category);
    }

    public void delete(List<String> ids) {
        this.vectorStore.delete(ids);
    }

    public void delete(List<String> ids, String category) {
        this.vectorStore.delete(ids, category);
    }

    public void deleteWhere(List<Map<String, String>> whereList) {
        this.vectorStore.deleteWhere(whereList);
    }

    public void deleteWhere(List<Map<String, String>> whereList, String category) {
        this.vectorStore.deleteWhere(whereList, category);
    }

    public void deleteCollection(String category) {
        this.vectorStore.deleteCollection(category);
    }

    public List<IndexSearchData> searchByIds(List<String> ids, String category) {
        List<IndexRecord> fetch = fetch(ids, category);
        if (fetch == null) {
            return Collections.emptyList();
        }
        List<IndexSearchData> indexSearchDataList = fetch.stream().map(this::toIndexSearchData).collect(Collectors.toList());
        List<Future<IndexSearchData>> futureResultList = indexSearchDataList.stream()
                .map(indexSearchData -> executor.submit(() -> extendIndexSearchData(indexSearchData, category)))
                .collect(Collectors.toList());
        return futureResultList.stream().map(indexSearchDataFuture -> {
            try {
                return indexSearchDataFuture.get();
            } catch (Exception e) {
                log.error("indexData get error", e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<IndexSearchData> search(ChatCompletionRequest request) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        List<IndexSearchData> indexSearchDataList = search(lastMessage, request.getCategory());
        return indexSearchDataList;
    }

    public List<IndexSearchData> searchByContext(EnhanceChatCompletionRequest request) {
        List<ChatMessage> messages = request.getMessages();
        IntentResult intentResult = intentService.detectIntent(request);
        if (intentResult.getIndexSearchDataList() != null) {
            return intentResult.getIndexSearchDataList();
        }
        String question = null;
        if (intentResult.getStatus() != null && intentResult.getStatus().equals(IntentStatusEnum.CONTINUE.getName())) {
            if (intentResult.getContinuedIndex() != null) {
                ChatMessage chatMessage = messages.get(intentResult.getContinuedIndex());
                String content = chatMessage.getContent();
                String[] split = content.split("[， ,.。！!?？]");
                String source = Arrays.stream(split).filter(StoppingWordUtil::containsStoppingWorlds).findAny().orElse("");
                if (StrUtil.isBlank(source)) {
                    source = content;
                }
                if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                    source = "";
                }
                question = source + ChatCompletionUtil.getLastMessage(request);
            } else {
                List<ChatMessage> userMessages = messages.stream().filter(m -> m.getRole().equals("user")).collect(Collectors.toList());
                if (userMessages.size() > 1) {
                    question = userMessages.get(userMessages.size() - 2).getContent().trim();
                }
            }
        }
        if (question == null) {
            question = ChatCompletionUtil.getLastMessage(request);
        }
        return search(question, request.getCategory(), request.getUserId());
    }

    public List<IndexSearchData> searchByContext(ChatCompletionRequest request) {
        List<ChatMessage> messages = request.getMessages();
        IntentResult intentResult = intentService.detectIntent(request);
        if (intentResult.getIndexSearchDataList() != null) {
            return intentResult.getIndexSearchDataList();
        }
        String question = null;
        if (intentResult.getStatus() != null && intentResult.getStatus().equals(IntentStatusEnum.CONTINUE.getName())) {
            if (intentResult.getContinuedIndex() != null) {
                ChatMessage chatMessage = messages.get(intentResult.getContinuedIndex());
                String content = chatMessage.getContent();
                String[] split = content.split("[， ,.。！!?？]");
                String source = Arrays.stream(split).filter(StoppingWordUtil::containsStoppingWorlds).findAny().orElse("");
                if (StrUtil.isBlank(source)) {
                    source = content;
                }
                if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                    source = "";
                }
                question = source + ChatCompletionUtil.getLastMessage(request);
            } else {
                List<ChatMessage> userMessages = messages.stream().filter(m -> m.getRole().equals("user")).collect(Collectors.toList());
                if (userMessages.size() > 1) {
                    question = userMessages.get(userMessages.size() - 2).getContent().trim();
                }
            }
        }
        if (question == null) {
            question = ChatCompletionUtil.getLastMessage(request);
        }
        return search(question, request.getCategory());
    }

    public List<IndexSearchData> search(String question, String category, String usr) {
        int similarity_top_k = vectorStore.getConfig().getSimilarityTopK();
        double similarity_cutoff = vectorStore.getConfig().getSimilarityCutoff();
        if (usr != null) {
            VectorSettingsDao dao = new VectorSettingsDao();
            try {
                List<UserRagSetting> userRagVector = dao.getUserRagVector(category, usr);
                for (UserRagSetting userRagSetting : userRagVector) {
                    if (userRagSetting.getFileType().equals("vector-max-top")) {
                        similarity_top_k = userRagSetting.getChunkSize();
                        continue;
                    }
                    if (userRagSetting.getFileType().equals("distance")) {
                        similarity_cutoff = userRagSetting.getTemperature();
                        continue;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Map<String, String> where = new HashMap<>();
        category = ObjectUtils.defaultIfNull(category, vectorStore.getConfig().getDefaultCategory());
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where, category);
        Set<String> esIds = bigdataService.getIds(question, category);
        if (esIds != null && !esIds.isEmpty()) {
            Set<String> indexIds = indexSearchDataList.stream().map(IndexSearchData::getId).collect(Collectors.toSet());
            indexIds.retainAll(esIds);
            indexSearchDataList = indexSearchDataList.stream()
                    .filter(indexSearchData -> indexIds.contains(indexSearchData.getId()))
                    .collect(Collectors.toList());
        }
        String finalCategory = category;
        List<Future<IndexSearchData>> futureResultList = indexSearchDataList.stream()
                .map(indexSearchData -> executor.submit(() -> extendIndexSearchData(indexSearchData, finalCategory)))
                .collect(Collectors.toList());
        return futureResultList.stream().map(indexSearchDataFuture -> {
            try {
                return indexSearchDataFuture.get();
            } catch (Exception e) {
                log.error("indexData get error");
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<IndexSearchData> search(String question, String category) {

        int similarity_top_k = vectorStore.getConfig().getSimilarityTopK();
        double similarity_cutoff = vectorStore.getConfig().getSimilarityCutoff();
        Map<String, String> where = new HashMap<>();
        category = ObjectUtils.defaultIfNull(category, vectorStore.getConfig().getDefaultCategory());
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where, category);
        Set<String> esIds = bigdataService.getIds(question, category);
        if (esIds != null && !esIds.isEmpty()) {
            Set<String> indexIds = indexSearchDataList.stream().map(IndexSearchData::getId).collect(Collectors.toSet());
            indexIds.retainAll(esIds);
            indexSearchDataList = indexSearchDataList.stream()
                    .filter(indexSearchData -> indexIds.contains(indexSearchData.getId()))
                    .collect(Collectors.toList());
        }
        String finalCategory = category;
        List<Future<IndexSearchData>> futureResultList = indexSearchDataList.stream()
                .map(indexSearchData -> executor.submit(() -> extendIndexSearchData(indexSearchData, finalCategory)))
                .collect(Collectors.toList());
        return futureResultList.stream().map(indexSearchDataFuture -> {
            try {
                return indexSearchDataFuture.get();
            } catch (Exception e) {
                log.error("indexData get error");
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private IndexSearchData extendIndexSearchData(IndexSearchData indexSearchData, String category) {
        IndexSearchData extendedIndexSearchData = vectorCache.getFromVectorLinkCache(indexSearchData.getId());
        if (extendedIndexSearchData == null) {
            extendedIndexSearchData = extendText(indexSearchData, category);
            vectorCache.putToVectorLinkCache(indexSearchData.getId(), extendedIndexSearchData);
        }
        extendedIndexSearchData.setDistance(indexSearchData.getDistance());
        return extendedIndexSearchData;
    }

    public List<IndexSearchData> getDataList(int parentDepth, int childDepth, IndexSearchData data, String category) {
        List<IndexSearchData> dataList = new ArrayList<>();
        dataList.add(data);
        String parentId = data.getParentId();
        for (int i = 0; i < parentDepth; i++) {
            IndexSearchData parentData = getParentIndex(parentId, category);
            if (parentData != null) {
                dataList.add(0, parentData);
            } else {
                break;
            }
        }
        parentId = data.getId();
        for (int i = 0; i < childDepth; i++) {
            IndexSearchData childData = getChildIndex(parentId, category);
            if (childData != null) {
                dataList.add(childData);
            } else {
                break;
            }
        }
        return dataList;
    }

    public List<List<IndexSearchData>> search(String question, int similarity_top_k, double similarity_cutoff,
                                              Map<String, String> where, String category, int parentDepth, int childDepth) {
        List<List<IndexSearchData>> results = new ArrayList<>();
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where, category);
        Set<String> esIds = bigdataService.getIds(question, category);
        if (esIds != null && !esIds.isEmpty()) {
            Set<String> indexIds = indexSearchDataList.stream().map(IndexSearchData::getId).collect(Collectors.toSet());
            indexIds.retainAll(esIds);
            indexSearchDataList = indexSearchDataList.stream()
                    .filter(indexSearchData -> indexIds.contains(indexSearchData.getId()))
                    .collect(Collectors.toList());
        }
        for (IndexSearchData indexSearchData : indexSearchDataList) {
            List<IndexSearchData> dataList = vectorCache.getFromVectorListCache(indexSearchData);
            if (dataList == null) {
                dataList = getDataList(parentDepth, childDepth, indexSearchData, category);
                vectorCache.putToVectorListCache(indexSearchData, dataList);
            }
            results.add(dataList);
        }
        return results;
    }

    public List<IndexSearchData> search(String question, int similarity_top_k, double similarity_cutoff,
                                        Map<String, String> where, String category) {
        List<IndexSearchData> result = new ArrayList<>();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setText(question);
        queryCondition.setN(similarity_top_k);
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = this.query(queryCondition, category);
        for (IndexRecord indexRecord : indexRecords) {
            if (indexRecord.getDistance() > similarity_cutoff) {
                continue;
            }
            IndexSearchData indexSearchData = toIndexSearchData(indexRecord);
            result.add(indexSearchData);
        }
        return result;
    }

    public IndexSearchData toIndexSearchData(IndexRecord indexRecord) {
        if (indexRecord == null) {
            return null;
        }
        IndexSearchData indexSearchData = new IndexSearchData();
        indexSearchData.setId(indexRecord.getId());
        indexSearchData.setText(indexRecord.getDocument());
        indexSearchData.setCategory((String) indexRecord.getMetadata().get("category"));
        indexSearchData.setLevel((String) indexRecord.getMetadata().get("level"));
        indexSearchData.setFileId((String) indexRecord.getMetadata().get("file_id"));
        String filename = (String) indexRecord.getMetadata().get("filename");
        Long seq = indexRecord.getMetadata().get("seq") == null ? 0L : Long.parseLong((String) indexRecord.getMetadata().get("seq"));
        indexSearchData.setSeq(seq);
        if (filename != null) {
            indexSearchData.setFilename(Collections.singletonList(filename));
        }
        if (indexRecord.getMetadata().get("filepath") != null) {
            indexSearchData.setFilepath(Collections.singletonList((String) indexRecord.getMetadata().get("filepath")));
        }
        indexSearchData.setImage((String) indexRecord.getMetadata().get("image"));
        indexSearchData.setDistance(indexRecord.getDistance());
        indexSearchData.setParentId((String) indexRecord.getMetadata().get("parent_id"));
        return indexSearchData;
    }

    public IndexSearchData getParentIndex(String parentId) {
        return getParentIndex(parentId, vectorStore.getConfig().getDefaultCategory());
    }

    public IndexSearchData getParentIndex(String parentId, String category) {
        if (parentId == null) {
            return null;
        }
        return toIndexSearchData(this.fetch(parentId, category));
    }

    public IndexSearchData getChildIndex(String parentId, String category) {
        IndexSearchData result = null;
        if (parentId == null) {
            return null;
        }
        Map<String, String> where = new HashMap<>();
        where.put("parent_id", parentId);
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setWhere(where);
        queryCondition.setN(1);
        List<IndexRecord> indexRecords = this.query(queryCondition, category);
        if (indexRecords != null && !indexRecords.isEmpty()) {
            result = toIndexSearchData(indexRecords.get(0));
        }
        return result;
    }

    public IndexSearchData extendText(IndexSearchData data) {
        return extendText(data, vectorStore.getConfig().getDefaultCategory());
    }

    public IndexSearchData extendText(IndexSearchData data, String category) {
        int parentDepth = vectorStore.getConfig().getParentDepth();
        int childDepth = vectorStore.getConfig().getChildDepth();
        return extendText(parentDepth, childDepth, data, category);
    }

    public IndexSearchData extendText(int parentDepth, int childDepth, IndexSearchData data, String category) {
        String text = data.getText().trim();
        String splitChar = "";
        if (data.getFilename() != null && data.getFilename().size() == 1
                && data.getFilename().get(0).isEmpty()) {
            splitChar = "\n";
        }

        String parentId = data.getParentId();
        int parentCount = 0;
        for (int i = 0; i < parentDepth; i++) {
            IndexSearchData parentData = getParentIndex(parentId, category);
            if (parentData != null) {
                text = parentData.getText() + splitChar + text;
                parentId = parentData.getParentId();
                parentCount++;
            } else {
                break;
            }
        }
        if (parentCount < parentDepth) {
            childDepth = childDepth + parentDepth - parentCount;
        }
        parentId = data.getId();
        for (int i = 0; i < childDepth; i++) {
            IndexSearchData childData = getChildIndex(parentId, category);
            if (childData != null) {
                text = text + splitChar + childData.getText();
                parentId = childData.getId();
            } else {
                break;
            }
        }
        data.setText(text);
        return data;
    }

    public List<String> getImageFiles(IndexSearchData indexData) {
        List<String> imageList = null;

        if (indexData.getImage() != null && !indexData.getImage().isEmpty()) {
            imageList = new ArrayList<>();
            List<JsonObject> imageObjectList = gson.fromJson(indexData.getImage(), new TypeToken<List<JsonObject>>() {
            }.getType());
            for (JsonObject image : imageObjectList) {
                String url = image.get("path").getAsString();
                imageList.add(url);
            }
        }
        return imageList;
    }

    public List<VectorCollection> listCollections() {
        return this.vectorStore.listCollections();
    }
}
