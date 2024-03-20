package ai.vector;

import ai.embedding.Embeddings;
import ai.migrate.pojo.VectorStoreConfig;
import ai.vector.impl.ChromaVectorStore;
import ai.vector.impl.PineconeVectorStore;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;

import java.util.Collections;
import java.util.List;

public class VectorStoreService {
    private VectorStore vectorStore;

    public VectorStoreService(VectorStoreConfig config, Embeddings embeddingFunction) {
        if (config.getType().equalsIgnoreCase(VectorStoreConstant.VECTOR_STORE_CHROMA)) {
            this.vectorStore = new ChromaVectorStore(config, embeddingFunction);
        } else if (config.getType().equalsIgnoreCase(VectorStoreConstant.VECTOR_STORE_PINECONE)) {
            this.vectorStore = new PineconeVectorStore(config, embeddingFunction);
        } else {
            throw new IllegalArgumentException("Unsupported vector store type: " + config.getType());
        }
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        this.vectorStore.upsert(upsertRecords);
    }

    public List<IndexRecord> query(QueryCondition queryCondition) {
        return this.vectorStore.query(queryCondition);
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

    public List<IndexRecord> fetch(List<String> ids) {
        return this.vectorStore.fetch(ids);
    }
}
