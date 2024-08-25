package ai.bigdata;


import ai.bigdata.pojo.TextIndexData;
import ai.manager.BigdataManager;

import java.util.List;
import java.util.Set;

public class BigdataService {
    private static final IBigdata adapter;

    static {
        adapter = BigdataManager.getInstance().getBigdata();
    }

    public boolean upsert(TextIndexData data) {
        return adapter.upsert(data);
    }

    public List<TextIndexData> search(String keyword, String category) {
        return adapter.search(keyword, category);
    }

    public boolean delete(String category) {
        if (adapter == null) {
            return false;
        }
        return adapter.delete(category);
    }

    public Set<String> getIds(String keyword, String category) {
        if (adapter == null) {
            return null;
        }
        return this.search(keyword, category).stream()
                .map(TextIndexData::getId)
                .collect(java.util.stream.Collectors.toSet());
    }
}
