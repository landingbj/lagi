package ai.common.pojo;

import com.google.gson.Gson;
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
        private static final Gson gson = new Gson();
        private String text;
        private List<Image> images;
        @Override
        public Document clone() {
            try {
                return gson.fromJson(gson.toJson(this), FileChunkResponse.Document.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Data
    public static class Image {
        private String path;
    }
}
