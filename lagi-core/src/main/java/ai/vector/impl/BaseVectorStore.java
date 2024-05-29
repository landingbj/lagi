package ai.vector.impl;

import ai.common.pojo.VectorStoreConfig;
import ai.vector.VectorStore;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BaseVectorStore implements VectorStore {

    protected VectorStoreConfig config;

    public VectorStoreConfig getConfig() {
        return config;
    }

    @Override
    public void upsert(List<UpsertRecord> upsertRecords) {

    }

    @Override
    public void upsert(List<UpsertRecord> upsertRecords, String category) {

    }

    @Override
    public List<IndexRecord> query(QueryCondition queryCondition) {
        return Collections.emptyList();
    }

    @Override
    public List<IndexRecord> query(QueryCondition queryCondition, String category) {
        return Collections.emptyList();
    }

    @Override
    public List<IndexRecord> fetch(List<String> ids) {
        return Collections.emptyList();
    }

    @Override
    public List<IndexRecord> fetch(List<String> ids, String category) {
        return Collections.emptyList();
    }

    @Override
    public void delete(List<String> ids) {

    }

    @Override
    public void delete(List<String> ids, String category) {

    }

    @Override
    public void deleteWhere(List<Map<String, String>> where) {

    }

    @Override
    public void deleteWhere(List<Map<String, String>> whereList, String category) {

    }
}
