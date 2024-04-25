package ai.servlet.dto;

import ai.vector.pojo.UpsertRecord;

import java.util.List;

public class VectorUpsertRequest {
    private String category;
    private Boolean isContextLinked;
    private List<UpsertRecord> data;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<UpsertRecord> getData() {
        return data;
    }

    public void setData(List<UpsertRecord> data) {
        this.data = data;
    }

    public Boolean getContextLinked() {
        return isContextLinked;
    }

    public void setContextLinked(Boolean contextLinked) {
        isContextLinked = contextLinked;
    }

    @Override
    public String toString() {
        return "VectorUpsertRequest{" +
                "category='" + category + '\'' +
                ", isContextLinked=" + isContextLinked +
                ", data=" + data +
                '}';
    }
}
