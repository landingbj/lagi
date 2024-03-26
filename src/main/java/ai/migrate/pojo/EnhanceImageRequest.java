package ai.migrate.pojo;

public class EnhanceImageRequest {
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "EnhanceImageRequest [imageUrl=" + imageUrl + "]";
    }
}
