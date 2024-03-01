package ai.migrate.pojo;

import java.util.List;

public class VideoEnhance {
    private List<Backend> backends;

    public List<Backend> getBackends() {
        return backends;
    }

    public void setBackends(List<Backend> backends) {
        this.backends = backends;
    }

    @Override
    public String toString() {
        return "VideoEnhance [backends=" + backends + "]";
    }
}