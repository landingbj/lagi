package ai.openai.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MultiModalContent {
    private String type;
    private String text;
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    @Data
    public static class ImageUrl {
        private String url;
        private String detail;
    }
}
