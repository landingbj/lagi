package ai.migrate.pojo;

import java.util.List;

public class ExtractContentResponse {
    private String status;
    private String filepath;
    private List<Document> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Document> getData() {
        return data;
    }

    public void setData(List<Document> data) {
        this.data = data;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public String toString() {
        return "ExtractContentResponse [status=" + status + ", filepath=" + filepath + ", data=" + data + "]";
    }
}
