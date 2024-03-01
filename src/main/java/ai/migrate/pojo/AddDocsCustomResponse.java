package ai.migrate.pojo;

import java.util.List;

public class AddDocsCustomResponse {
	private String status;
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

    @Override
    public String toString() {
        return "AddDocsCustomResponse [status=" + status + ", data=" + data + "]";
    }
}
