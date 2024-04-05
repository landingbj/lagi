package ai.migrate.pojo;

import java.util.Map;

public class FileInfo {
    private String text;
    private String embedding_id;
    private Map<String, Object> metadatas;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmbedding_id() {
        return embedding_id;
    }

    public void setEmbedding_id(String embedding_id) {
        this.embedding_id = embedding_id;
    }

    public Map<String, Object> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(Map<String, Object> metadatas) {
        this.metadatas = metadatas;
    }

    @Override
    public String toString() {
        return "FileInfo [text=" + text + ", embedding_id=" + embedding_id + ", metadatas=" + metadatas + "]";
    }

}
