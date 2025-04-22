package ai.video.pojo;

import lombok.Data;

@Data
public class VideoResponse {
    private String status;
    private String svdVideoUrl;
    private String data;
    private String type;
}
