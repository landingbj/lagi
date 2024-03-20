package ai.migrate.pojo;

import java.util.List;

public class LLM {
    private EmbeddingConfig embedding;
    private List<Backend> backends;

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
                '}';
    }
}