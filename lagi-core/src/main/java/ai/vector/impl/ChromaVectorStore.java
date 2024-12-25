package ai.vector.impl;

import ai.embedding.Embeddings;
import ai.common.pojo.VectorStoreConfig;
import ai.utils.HttpUtil;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection;
import tech.amikos.chromadb.EmbeddingFunction;
import tech.amikos.chromadb.handler.*;
import tech.amikos.chromadb.model.QueryEmbedding;

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

    /**
     * $in
     * @param queryCondition
     * @param category
     * @return
     */
    public List<IndexRecord> query(QueryCondition queryCondition, String category,Map<String, Object> where) {
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
        Collection.QueryResponse qr = null;
        try {
            qr = reconstitutionQuery(queryTexts, n, where,category);
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

    private Collection.QueryResponse reconstitutionQuery(List<String> queryTexts, Integer nResults, Map<String, Object> where,String category) throws ApiException {
        QueryEmbedding body = new QueryEmbedding();
        body.queryEmbeddings((List)this.embeddingFunction.createEmbedding(queryTexts));
        body.nResults(nResults);
        body.include(null);
        if (where != null) {
            body.where(where);
        }

        String apiurl = config.getUrl()+"/api/v1/collections/"+category;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpGet(apiurl, headers,10 * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
        com.google.gson.JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();
        String id = jsonObject.get("id").getAsString();

        String apiurl1 = config.getUrl()+"/api/v1/collections/"+id+"/query";
        Map<String, String> headers1 = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String jsonResult1 = null;
        try {
            jsonResult1 = HttpUtil.httpPost(apiurl1, headers1,body, 10 * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
        return (Collection.QueryResponse)(new Gson()).fromJson(jsonResult1, Collection.QueryResponse.class);
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
