package ai.vector;

import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.UpsertRecord;

import java.util.List;

public interface VectorStore {
    void upsert(List<UpsertRecord> upsertRecords);

    void upsert(List<UpsertRecord> upsertRecords, String category);

    List<IndexRecord> query(QueryCondition queryCondition);

    List<IndexRecord> query(QueryCondition queryCondition, String category);

    List<IndexRecord> fetch(List<String> ids);

    List<IndexRecord> fetch(List<String> ids, String category);
}
