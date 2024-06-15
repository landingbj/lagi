package ai.video.pojo;

import lombok.*;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VideoJobQueryResponse {
    private String taskId;
    //        QUEUING：任务排队中 0
    //        PROCESSING：异步处理中 1
    //        PROCESS_SUCCESS：处理成功 2
    //        PROCESS_FAILED：处理失败 3
    //        TIMEOUT_FAILED：任务超时未处理完成 4
    //        LIMIT_RETRY_FAILED：超过最大重试次数 5
    private Integer status;
    private String progress;
    private String videoUrl;
}
