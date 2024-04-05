package ai.migrate.pojo;

public class GenerateVideoRequest {
    private String imageUrl;
    private String prompt;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "GenerateVideoRequest [imageUrl=" + imageUrl + ", prompt=" + prompt + "]";
    }
}
