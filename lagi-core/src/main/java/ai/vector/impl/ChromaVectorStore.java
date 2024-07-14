package ai.vector.impl;

import ai.embedding.Embeddings;
import ai.common.pojo.VectorStoreConfig;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;
import com.google.gson.internal.LinkedTreeMap;
import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection;
import tech.amikos.chromadb.EmbeddingFunction;
import tech.amikos.chromadb.handler.ApiException;

import java.util.*;

public class ChromaVectorStore extends BaseVectorStore {
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
    private Map<String, String> colMetadata;
    private Client client;

    public ChromaVectorStore(VectorStoreConfig config, Embeddings embeddingFunction) {
        this.config = config;
        this.embeddingFunction = new CustomEmbeddingFunction(embeddingFunction);
        client = new Client(config.getUrl());
        colMetadata = new LinkedTreeMap<>();
        colMetadata.put("hnsw:space", config.getMetric());
        colMetadata.put("embedding_function", this.embeddingFunction.getClass().getName());
    }

    private Collection getCollection(String category) {
        Collection collection = null;
        try {
            collection = client.createCollection(category, colMetadata, true, this.embeddingFunction);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return collection;
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        upsert(upsertRecords, this.config.getDefaultCategory());
    }

    public void upsert(List<UpsertRecord> upsertRecords, String category) {
        List<String> documents = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (UpsertRecord upsertRecord : upsertRecords) {
            documents.add(upsertRecord.getDocument());
            metadatas.add(upsertRecord.getMetadata());
            ids.add(upsertRecord.getId());
        }
        List<List<Float>> embeddings = this.embeddingFunction.createEmbedding(documents);
        Collection collection = getCollection(category);
        try {
            collection.upsert(embeddings, metadatas, documents, ids);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public List<IndexRecord> query(QueryCondition queryCondition) {
        return query(queryCondition, this.config.getDefaultCategory());
    }

    public List<IndexRecord> query(QueryCondition queryCondition, String category) {
        List<IndexRecord> result = new ArrayList<>();
        Collection collection = getCollection(category);
        Collection.GetResult gr;
        if (queryCondition.getText() == null) {
            try {
                gr = collection.get(null, queryCondition.getWhere(), null);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
            return getIndexRecords(result, gr);
        }
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

    private List<IndexRecord> getIndexRecords(List<IndexRecord> result, Collection.GetResult gr) {
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

    public List<IndexRecord> fetch(List<String> ids) {
        return fetch(ids, this.config.getDefaultCategory());
    }

    public List<IndexRecord> fetch(List<String> ids, String category) {
        List<IndexRecord> result = new ArrayList<>();
        Collection.GetResult gr;
        Collection collection = getCollection(category);
        try {
            gr = collection.get(ids, null, null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return getIndexRecords(result, gr);
    }

    public List<IndexRecord> fetch(Map<String, String> where) {
        return fetch(where, this.config.getDefaultCategory());
    }

    public List<IndexRecord> fetch(Map<String, String> where, String category) {
        List<IndexRecord> result = new ArrayList<>();
        Collection.GetResult gr;
        Collection collection = getCollection(category);
        try {
            gr = collection.get(null, where, null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return getIndexRecords(result, gr);
    }

    public void delete(List<String> ids) {
        this.delete(ids, this.config.getDefaultCategory());
    }

    public void delete(List<String> ids, String category) {
        Collection collection = getCollection(category);
        try {
            collection.deleteWithIds(ids);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteWhere(List<Map<String, String>> whereList) {
        deleteWhere(whereList, this.config.getDefaultCategory());
    }

    @Override
    public void deleteWhere(List<Map<String, String>> whereList, String category) {
        Collection collection = getCollection(category);
        try {
            for (Map<String, String> where : whereList) {
                collection.deleteWhere(where);
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCollection(String category) {
        try {
            for (VectorCollection vectorCollection : listCollections()) {
                if (vectorCollection.getCategory().equals(category)) {
                    client.deleteCollection(category);
                    break;
                }
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<VectorCollection> listCollections() {
        List<VectorCollection> result = new ArrayList<>();
        try {
            List<Collection> collections = client.listCollections();
            for (Collection collection : collections) {
                VectorCollection vectorCollection = VectorCollection.builder()
                        .category(collection.getName())
                        .vectorCount(collection.count())
                        .build();
                result.add(vectorCollection);
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
