package ai.video.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VideoJobResponse {
    private String status;
    private String jobId;
    private String data;
}
