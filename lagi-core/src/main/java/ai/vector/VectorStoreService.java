package ai.vector;

import ai.common.pojo.*;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.intent.IntentService;
import ai.intent.enums.IntentStatusEnum;
import ai.intent.impl.SampleIntentServiceImpl;
import ai.intent.pojo.IntentResult;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatMessage;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.impl.BaseVectorStore;
import ai.vector.impl.ChromaVectorStore;
import ai.vector.impl.PineconeVectorStore;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VectorStoreService {
    private final Gson gson = new Gson();
    private final BaseVectorStore vectorStore;
    private final FileService fileService = new FileService();

    private IntentService intentService = new SampleIntentServiceImpl();


    public VectorStoreService() {
        this.vectorStore = VectorStoreManager.getVectorStore();
    }

    public VectorStoreConfig getVectorStoreConfig() {
        return vectorStore.getConfig();
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        List<FileChunkResponse.Document> docs;
        FileChunkResponse response = fileService.extractContent(file);
        if (response != null && response.getStatus().equals("success")) {
            docs = response.getData();
        } else {
            docs = fileService.splitChunks(file, 512);
        }
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
        upsertFileVectors(fileList, category);
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
            metadata.put(entry.getKey(), entry.getValue().toString());
        }
        upsertRecord.setMetadata(metadata);

        return upsertRecord;
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        this.vectorStore.upsert(upsertRecords);
    }

    public void upsert(List<UpsertRecord> upsertRecords, String category) {
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

    public List<IndexSearchData> search(ChatCompletionRequest request) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        List<IndexSearchData> indexSearchDataList = search(lastMessage, request.getCategory());
        return indexSearchDataList;
    }

    public List<IndexSearchData> searchByContext(ChatCompletionRequest request) {
        List<ChatMessage> messages = request.getMessages();
        List<String> collect = messages.stream().map(ChatMessage::getContent).collect(Collectors.toList());
        IntentResult intentResult = intentService.detectIntent(collect);
        String question = null;
        if(intentResult.getStatus() != null && intentResult.getStatus().equals(IntentStatusEnum.CONTINUE.getName())) {
            List<ChatMessage> userMessages = messages.stream().filter(m -> m.getRole().equals("user")).collect(Collectors.toList());
            if(userMessages.size() > 1) {
                question = userMessages.get(userMessages.size() - 2).getContent().trim();
            }
        }
        if(question == null) {
            question = ChatCompletionUtil.getLastMessage(request);
        }
        return search(question, request.getCategory());
    }

    public List<IndexSearchData> search(String question, String category) {
        int similarity_top_k = vectorStore.getConfig().getSimilarityTopK();
        double similarity_cutoff = vectorStore.getConfig().getSimilarityCutoff();
        int parentDepth = vectorStore.getConfig().getParentDepth();
        int childDepth = vectorStore.getConfig().getChildDepth();
        Map<String, String> where = new HashMap<>();
        List<IndexSearchData> result = new ArrayList<>();
        category = ObjectUtils.defaultIfNull(category, vectorStore.getConfig().getDefaultCategory());
        List<IndexSearchData> indexSearchDataList = search(question, similarity_top_k, similarity_cutoff, where, category);
        for (IndexSearchData indexSearchData : indexSearchDataList) {
            result.add(extendText(parentDepth, childDepth, indexSearchData, category));
        }
        return result;
    }

    private List<IndexSearchData> search(String question, int similarity_top_k, double similarity_cutoff,
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

    private IndexSearchData toIndexSearchData(IndexRecord indexRecord) {
        if (indexRecord == null) {
            return null;
        }
        IndexSearchData indexSearchData = new IndexSearchData();
        indexSearchData.setId(indexRecord.getId());
        indexSearchData.setText(indexRecord.getDocument());
        indexSearchData.setCategory((String) indexRecord.getMetadata().get("category"));
        indexSearchData.setLevel((String) indexRecord.getMetadata().get("level"));
        if (!"system".equals(indexSearchData.getLevel())) {
            indexSearchData.setFileId((String) indexRecord.getMetadata().get("file_id"));
            if (indexRecord.getMetadata().get("filename") != null) {
                indexSearchData.setFilename(Collections.singletonList((String) indexRecord.getMetadata().get("filename")));
            }
            if (indexRecord.getMetadata().get("filepath") != null) {
                indexSearchData.setFilepath(Collections.singletonList((String) indexRecord.getMetadata().get("filepath")));
            }
        }
        indexSearchData.setImage((String) indexRecord.getMetadata().get("image"));
        indexSearchData.setDistance(indexRecord.getDistance());
        indexSearchData.setParentId((String) indexRecord.getMetadata().get("parent_id"));
        return indexSearchData;
    }

    private IndexSearchData getParentIndex(String parentId, String category) {
        if (parentId == null) {
            return null;
        }
        return toIndexSearchData(this.fetch(parentId, category));
    }

    private IndexSearchData getChildIndex(String parentId, String category) {
        IndexSearchData result = null;
        if (parentId == null) {
            return null;
        }
        Map<String, String> where = new HashMap<>();
        where.put("parent_id", parentId);
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setWhere(where);
        List<IndexRecord> indexRecords = this.query(queryCondition, category);
        if (indexRecords != null && !indexRecords.isEmpty()) {
            result = toIndexSearchData(indexRecords.get(0));
        }
        return result;
    }

    private IndexSearchData extendText(int parentDepth, int childDepth, IndexSearchData data, String category) {
        String text = data.getText();
        String parentId = data.getParentId();
        int parentCount = 0;
        for (int i = 0; i < parentDepth; i++) {
            IndexSearchData parentData = getParentIndex(parentId, category);
            if (parentData != null) {
                text = parentData.getText() + text;
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
                text = text + childData.getText();
                parentId = childData.getId();
            } else {
                break;
            }
        }
        data.setText(text);
        return data;
    }
}
