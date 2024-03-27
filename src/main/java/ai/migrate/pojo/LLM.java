package ai.migrate.pojo;

import java.util.List;

public class LLM {
    private EmbeddingConfig embedding;
    private List<Backend> backends;

    private String steam_backend;

    public String getSteam_backend() {
        return steam_backend;
    }

    public void setSteam_backend(String steam_backend) {
        this.steam_backend = steam_backend;
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
                ", steam_backend='" + steam_backend + '\'' +
                '}';
    }
}