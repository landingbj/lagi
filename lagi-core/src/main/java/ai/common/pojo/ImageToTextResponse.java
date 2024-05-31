package ai.common.pojo;

import lombok.Data;

@Data
public class ImageToTextResponse {
    private String classification;
    private String caption;
    private String samUrl;
    private String status;

    public static ImageToTextResponse error() {
        ImageToTextResponse res = new ImageToTextResponse();
        res.setStatus("failed");
        return res;
    }

    public static ImageToTextResponse success(String caption) {
        ImageToTextResponse res = new ImageToTextResponse();
        res.setStatus("success");
        res.setCaption(caption);
        return res;
    }
}
