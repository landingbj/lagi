package ai.common.pojo;

import java.util.List;

public class FileChunkResponse {
    private String status;
    private List<Document> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Document> getData() {
        return data;
    }

    public void setData(List<Document> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ExtractContentResponse [status=" + status + ", data=" + data + "]";
    }

    public static class Document {
        private String text;
        private List<Image> images;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Image> getImages() {
            return images;
        }

        public void setImage(List<Image> images) {
            this.images = images;
        }

        @Override
        public String toString() {
            return "Document [text=" + text + ", image=" + images + "]";
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
