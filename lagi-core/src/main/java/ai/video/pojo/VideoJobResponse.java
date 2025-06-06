package ai.video.pojo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VideoJobResponse {
    private String status;
    private String jobId;
    private String data;
    private String message;
    // 修改构造方法为public
    public VideoJobResponse(String status, String jobId, String data ,String message) {
        this.status = status;
        this.jobId = jobId;
        this.data = data;
        this.message = message;
    }
    public VideoJobResponse() {
    }
}
