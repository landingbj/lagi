package ai.common.pojo;

import java.util.List;

public class VideoGeneration {
    private List<Backend> backends;

    public List<Backend> getBackends() {
        return backends;
    }

    public void setBackends(List<Backend> backends) {
        this.backends = backends;
    }

    @Override
    public String toString() {
        return "VideoGeneration [backends=" + backends + "]";
    }
}