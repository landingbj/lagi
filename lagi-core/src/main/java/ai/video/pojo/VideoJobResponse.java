package ai.video.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VideoJobResponse {
    private String jobId;
    private String data;
}
