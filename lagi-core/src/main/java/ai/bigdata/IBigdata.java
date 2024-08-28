package ai.bigdata;


import ai.bigdata.pojo.TextIndexData;

import java.util.List;

public interface IBigdata {
    boolean upsert(TextIndexData data);

    List<TextIndexData> search(String keyword, String category);

    boolean delete(String category);
}
