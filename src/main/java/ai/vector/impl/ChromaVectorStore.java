package ai.vector.impl;

import ai.embedding.Embeddings;
import ai.migrate.pojo.VectorStoreConfig;
import ai.vector.VectorStore;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import com.google.gson.internal.LinkedTreeMap;
import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection;
import tech.amikos.chromadb.EmbeddingFunction;
import tech.amikos.chromadb.handler.ApiException;

import java.util.*;

public class ChromaVectorStore implements VectorStore {
    public static class CustomEmbeddingFunction implements EmbeddingFunction {
        private Embeddings ef;

        public CustomEmbeddingFunction(Embeddings ef) {
            this.ef = ef;
        }

        @Override
        public List<List<Float>> createEmbedding(List<String> list) {
            return this.ef.createEmbedding(list);
        }

        @Override
        public List<List<Float>> createEmbedding(List<String> list, String s) {
            return null;
        }
    }

    private CustomEmbeddingFunction embeddingFunction;
    private Collection collection;

    public ChromaVectorStore(VectorStoreConfig config, Embeddings embeddingFunction) {
        this.embeddingFunction = new CustomEmbeddingFunction(embeddingFunction);
        Client client = new Client(config.getUrl());
        Map<String, String> colMetadata = new LinkedTreeMap<>();
        colMetadata.put("hnsw:space", config.getMetric());
        colMetadata.put("embedding_function", this.embeddingFunction.getClass().getName());
        try {
            collection = client.createCollection(config.getDefault_category(), colMetadata,
                    true, this.embeddingFunction);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        List<String> documents = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (UpsertRecord upsertRecord : upsertRecords) {
            documents.add(upsertRecord.getDocument());
            metadatas.add(upsertRecord.getMetadata());
            ids.add(upsertRecord.getId());
        }
        List<List<Float>> embeddings = this.embeddingFunction.createEmbedding(documents);
        try {
            this.collection.upsert(embeddings, metadatas, documents, ids);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public List<IndexRecord> query(QueryCondition queryCondition) {
        List<IndexRecord> result = new ArrayList<>();
        List<String> queryTexts = Collections.singletonList(queryCondition.getText());
        Integer n = queryCondition.getN();
        Map<String, String> where = queryCondition.getWhere();
        Collection.QueryResponse qr = null;
        try {
            qr = collection.query(queryTexts, n, where, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < qr.getDocuments().size(); i++) {
            for (int j = 0; j < qr.getDocuments().get(i).size(); j++) {
                IndexRecord indexRecord = IndexRecord.newBuilder()
                        .withDocument(qr.getDocuments().get(i).get(j))
                        .withId(qr.getIds().get(i).get(j))
                        .withMetadata(qr.getMetadatas().get(i).get(j))
                        .withDistance(qr.getDistances().get(i).get(j))
                        .build();
                result.add(indexRecord);
            }
        }
        return result;
    }

    public List<IndexRecord> fetch(List<String> ids) {
        List<IndexRecord> result = new ArrayList<>();
        Collection.GetResult gr;
        try {
            gr = collection.get(ids, null, null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < gr.getDocuments().size(); i++) {
            IndexRecord indexRecord = IndexRecord.newBuilder()
                    .withDocument(gr.getDocuments().get(i))
                    .withId(gr.getIds().get(i))
                    .withMetadata(gr.getMetadatas().get(i))
                    .build();
            result.add(indexRecord);
        }
        return result;
    }

    public void update() {
    }

    public void delete() {
    }
}
