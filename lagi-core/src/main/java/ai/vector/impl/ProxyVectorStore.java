package ai.vector.impl;

import ai.llm.utils.LimitedCapacitySequentialLock;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import ai.vector.pojo.VectorCollection;

import java.util.List;
import java.util.Map;

public class ProxyVectorStore extends BaseVectorStore{

    private final BaseVectorStore vectorStore;


    private LimitedCapacitySequentialLock lock;

    public ProxyVectorStore(BaseVectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.config = vectorStore.getConfig();
        if(config.getConcurrency() == null) {
            this.lock = new LimitedCapacitySequentialLock(Integer.MAX_VALUE);
        } else {
            this.lock = new LimitedCapacitySequentialLock(config.getConcurrency());
        }
    }

    @Override
    public void upsert(List<UpsertRecord> upsertRecords) {
        this.lock.acquire();
        try {
            this.vectorStore.upsert(upsertRecords);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public void upsert(List<UpsertRecord> upsertRecords, String category) {
        this.lock.acquire();
        try {
            this.vectorStore.upsert(upsertRecords, category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<IndexRecord> query(QueryCondition queryCondition) {
        this.lock.acquire();
        try {
            return this.vectorStore.query(queryCondition);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<IndexRecord> query(QueryCondition queryCondition, String category) {
        this.lock.acquire();
        try {
            return this.vectorStore.query(queryCondition, category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<IndexRecord> fetch(List<String> ids) {
        this.lock.acquire();
        try {
            return this.vectorStore.fetch(ids);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<IndexRecord> fetch(Map<String, String> where) {
        this.lock.acquire();
        try {
            return this.vectorStore.fetch(where);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<IndexRecord> fetch(Map<String, String> where, String category) {
        this.lock.acquire();
        try {
            return this.vectorStore.fetch(where, category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<IndexRecord> fetch(List<String> ids, String category) {
        this.lock.acquire();
        try {
            return this.vectorStore.fetch(ids, category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public void delete(List<String> ids) {
        this.lock.acquire();
        try {
            this.vectorStore.delete(ids);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public void delete(List<String> ids, String category) {
        this.lock.acquire();
        try {
            this.vectorStore.delete(ids, category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public void deleteWhere(List<Map<String, String>> where) {
        this.lock.acquire();
        try {
            this.vectorStore.deleteWhere(where);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public void deleteWhere(List<Map<String, String>> whereList, String category) {
        this.lock.acquire();
        try {
            this.vectorStore.deleteWhere(whereList, category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public void deleteCollection(String category) {
        this.lock.acquire();
        try {
            this.vectorStore.deleteCollection(category);
        } finally {
            this.lock.release();
        }
    }

    @Override
    public List<VectorCollection> listCollections() {
        this.lock.acquire();
        try {
            return this.listCollections();
        } finally {
            this.lock.release();
        }
    }

}
