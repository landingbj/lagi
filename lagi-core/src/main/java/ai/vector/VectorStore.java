package ai.vector;

import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;

import java.util.List;
import java.util.Map;

public interface VectorStore {
    void upsert(List<UpsertRecord> upsertRecords);

    void upsert(List<UpsertRecord> upsertRecords, String category);

    List<IndexRecord> query(QueryCondition queryCondition);

    List<IndexRecord> query(QueryCondition queryCondition, String category);

    List<IndexRecord> fetch(List<String> ids);

    List<IndexRecord> fetch(List<String> ids, String category);

    List<IndexRecord> fetch(Map<String, String> where);

    List<IndexRecord> fetch(Map<String, String> where, String category);

    void delete(List<String> ids);

    void delete(List<String> ids, String category);

    void deleteWhere(List<Map<String, String>> where);

    void deleteWhere(List<Map<String, String>> whereList, String category);

    void deleteCollection(String category);

    List<VectorCollection> listCollections();
}
