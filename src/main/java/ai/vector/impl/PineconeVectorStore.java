package ai.vector.impl;

import ai.embedding.Embeddings;
import ai.migrate.pojo.VectorStoreConfig;
import ai.vector.VectorStore;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.proto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PineconeVectorStore implements VectorStore {
    private static final String DOCUMENT = "document";
    private PineconeClientConfig pineconeClientConfig;
    private PineconeClient pineconeClient;
    private Embeddings embeddingFunction;
    private VectorStoreConfig config;

    public PineconeVectorStore(VectorStoreConfig config, Embeddings embeddingFunction) {
        this.embeddingFunction = embeddingFunction;
        this.config = config;
        pineconeClientConfig = new PineconeClientConfig()
                .withApiKey(config.getApi_key())
                .withEnvironment(config.getEnvironment())
                .withProjectName(config.getProject_name());
        pineconeClient = new PineconeClient(pineconeClientConfig);
    }

    public static Struct generateMetadataStruct(Map<String, String> metadata) {
        Struct.Builder builder = Struct.newBuilder();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            builder.putFields(entry.getKey(), Value.newBuilder().setStringValue(entry.getValue()).build());
        }
        return builder.build();
    }

    public static Struct generateWhereStruct(Map<String, String> where) {
        Struct.Builder builder = Struct.newBuilder();
        for (Map.Entry<String, String> entry : where.entrySet()) {
            builder.putFields(entry.getKey(), Value.newBuilder().setStringValue(entry.getValue()).build());
        }
        return builder.build();
    }

    public void upsert(List<UpsertRecord> upsertRecords) {
        List<String> documents = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (UpsertRecord upsertRecord : upsertRecords) {
            documents.add(upsertRecord.getDocument());
            Map<String, String> metadata = upsertRecord.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            metadata.put(DOCUMENT, upsertRecord.getDocument());
            metadatas.add(metadata);
            ids.add(upsertRecord.getId());
        }
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName(this.config.getIndex_name());
        try (PineconeConnection connection = pineconeClient.connect(connectionConfig)) {
            List<List<Float>> embeddings = embeddingFunction.createEmbedding(documents);
            UpsertRequest.Builder builder = UpsertRequest.newBuilder()
                    .setNamespace(this.config.getDefault_category());
            for (int i = 0; i < ids.size(); i++) {
                Struct metadataStrut = generateMetadataStruct(metadatas.get(i));
                Vector vector = Vector.newBuilder()
                        .setId(ids.get(i))
                        .addAllValues(embeddings.get(i))
                        .setMetadata(metadataStrut)
                        .build();
                builder.addVectors(vector);
            }
            UpsertRequest upsertRequest = builder.build();
            UpsertResponse upsertResponse = connection.getBlockingStub().upsert(upsertRequest);
        } catch (PineconeException e) {
            e.printStackTrace();
        }
    }

    public List<IndexRecord> query(QueryCondition queryCondition) {
        String text = queryCondition.getText();
        Integer n = queryCondition.getN();
        Map<String, String> where = queryCondition.getWhere();
        Struct whereStruct = generateWhereStruct(where);
        List<IndexRecord> result = new ArrayList<>();
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName(this.config.getIndex_name());
        try (PineconeConnection connection = pineconeClient.connect(connectionConfig)) {
            Iterable<Float> iterable = embeddingFunction.createEmbedding(text);
            QueryRequest queryRequest = QueryRequest.newBuilder()
                    .addAllVector(iterable)
                    .setNamespace(this.config.getDefault_category())
                    .setTopK(n)
                    .setIncludeMetadata(true)
                    .setFilter(whereStruct)
                    .build();
            result = query(connection, queryRequest);
        } catch (PineconeException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<IndexRecord> fetch(List<String> ids) {
        List<IndexRecord> result = new ArrayList<>();
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName(this.config.getIndex_name());
        try (PineconeConnection connection = pineconeClient.connect(connectionConfig)) {
            for (String id : ids) {
                QueryRequest queryRequest = QueryRequest.newBuilder()
                        .setId(id)
                        .setNamespace(this.config.getDefault_category())
                        .setIncludeMetadata(true)
                        .setTopK(1)
                        .build();
                result.addAll(query(connection, queryRequest));
            }
        } catch (PineconeException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<IndexRecord> query(PineconeConnection connection, QueryRequest queryRequest) {
        List<IndexRecord> result = new ArrayList<>();
        QueryResponse queryResponse = connection.getBlockingStub().query(queryRequest);
        for (int i = 0; i < queryResponse.getMatchesCount(); i++) {
            Map<String, Value> fieldsMap = queryResponse.getMatches(i).getMetadata().getFieldsMap();
            String document = fieldsMap.get(DOCUMENT).getStringValue();
            String id = queryResponse.getMatches(i).getId();
            Map<String, Object> metadata = new HashMap<>();
            for (Map.Entry<String, Value> entry : fieldsMap.entrySet()) {
                if (entry.getKey().equals(DOCUMENT)) {
                    continue;
                }
                metadata.put(entry.getKey(), entry.getValue().getStringValue());
            }
            Float distance = queryResponse.getMatches(i).getScore();

            IndexRecord indexRecord = IndexRecord.newBuilder()
                    .withDocument(document)
                    .withId(id)
                    .withMetadata(metadata)
                    .withDistance(distance)
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
