package ai.migrate.pojo;

import java.util.List;

public class ImageEnhance {
    private List<Backend> backends;

    public List<Backend> getBackends() {
        return backends;
    }

    public void setBackends(List<Backend> backends) {
        this.backends = backends;
    }

    @Override
    public String toString() {
        return "ImageEnhance [backends=" + backends + "]";
    }
}