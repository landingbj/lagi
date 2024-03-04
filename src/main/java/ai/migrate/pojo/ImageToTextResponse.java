package ai.migrate.pojo;

public class ImageToTextResponse {
    private String classification;
    private String caption;
    private String samUrl;
    private String status;

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getSamUrl() {
        return samUrl;
    }

    public void setSamUrl(String samUrl) {
        this.samUrl = samUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
