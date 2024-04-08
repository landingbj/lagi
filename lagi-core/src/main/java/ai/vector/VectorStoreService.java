package ai.vector;

import ai.common.pojo.Document;
import ai.common.pojo.ExtractContentResponse;
import ai.common.pojo.FileInfo;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.common.pojo.VectorStoreConfig;
import ai.utils.LagiGlobal;
import ai.vector.impl.ChromaVectorStore;
import ai.vector.impl.PineconeVectorStore;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VectorStoreService {
    private final VectorStore vectorStore;
    private final FileService fileService = new FileService();

    public VectorStoreService() {
        this(LagiGlobal.getConfig().getVectorStore(), EmbeddingFactory.getEmbedding());
    }

    public VectorStoreService(VectorStoreConfig config, Embeddings embeddingFunction) {
        if (config.getType().equalsIgnoreCase(VectorStoreConstant.VECTOR_STORE_CHROMA)) {
            this.vectorStore = new ChromaVectorStore(config, embeddingFunction);
        } else if (config.getType().equalsIgnoreCase(VectorStoreConstant.VECTOR_STORE_PINECONE)) {
            this.vectorStore = new PineconeVectorStore(config, embeddingFunction);
        } else {
            throw new IllegalArgumentException("Unsupported vector store type: " + config.getType());
        }
    }

    public void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException {
        List<Document> docs;
        if (LagiGlobal.IMAGE_EXTRACT_ENABLE) {
            ExtractContentResponse response = fileService.extractContent(file);
            docs = response.getData();
        } else {
            docs = fileService.splitChunks(file, 512);
        }
        List<FileInfo> fileList = new ArrayList<>();
        for (Document doc : docs) {
            FileInfo fileInfo = new FileInfo();
            String embeddingId = UUID.randomUUID().toString().replace("-", "");
            fileInfo.setEmbedding_id(embeddingId);
            fileInfo.setText(doc.getText());
            Map<String, Object> tmpMetadatas = new HashMap<>(metadatas);
            if (doc.getImage() != null) {
                tmpMetadatas.put("image", doc.getImage());
            }
            fileInfo.setMetadatas(tmpMetadatas);
            fileList.add(fileInfo);
        }
        upsertFileVectors(fileList, category);
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
}
