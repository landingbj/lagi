package ai.migrate.pojo;

public class CaptionRequest {
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "CaptionRequest [imageUrl=" + imageUrl + "]";
    }
}
