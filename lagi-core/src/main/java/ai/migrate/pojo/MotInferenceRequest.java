package ai.migrate.pojo;

public class MotInferenceRequest {
    private String videoUrl;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public String toString() {
        return "MotInferenceRequest [videoUrl=" + videoUrl + "]";
    }
}
