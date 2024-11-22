package ai.common.pojo;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileChunkResponse {
    private String status;
    private List<Document> data;

    @Override
    public String toString() {
        return "ExtractContentResponse [status=" + status + ", data=" + data + "]";
    }

    @Data
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private String text;
        private List<Image> images;
        private Integer pageNo;
        private Boolean isStart = false;
        private List<Float> rect;

        public void setImage(List<Image> images) {
            this.images = images;
        }


    }

    public static class Image {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return "Image [path=" + path + "]";
        }
    }
}
