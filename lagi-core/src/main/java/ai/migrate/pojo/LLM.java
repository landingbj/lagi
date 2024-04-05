package ai.migrate.pojo;

import java.util.List;

public class LLM {
    private EmbeddingConfig embedding;
    private List<Backend> backends;

    private String stream_backend;

    public String getStream_backend() {
        return stream_backend;
    }

    public void setStream_backend(String stream_backend) {
        this.stream_backend = stream_backend;
    }

    public List<Backend> getBackends() {
        return backends;
    }

    public void setBackends(List<Backend> backends) {
        this.backends = backends;
    }

    public EmbeddingConfig getEmbedding() {
        return embedding;
    }

    public void setEmbedding(EmbeddingConfig embedding) {
        this.embedding = embedding;
    }

    @Override
    public String toString() {
        return "LLM{" +
                "embedding=" + embedding +
                ", backends=" + backends +
                ", steam_backend='" + stream_backend + '\'' +
                '}';
    }
}