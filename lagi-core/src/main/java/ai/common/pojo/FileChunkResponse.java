package ai.common.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileChunkResponse {
    private String status;
    private List<Document> data;
    private String msg;

    @Data
    public static class Document {
        private String text;
        private List<Image> images;
    }

    @Data
    public static class Image {
        private String path;
    }
}
