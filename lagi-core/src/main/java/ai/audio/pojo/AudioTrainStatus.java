package ai.audio.pojo;

import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AudioTrainStatus{
    private Integer status;
    private String message;
    private String version;
    private String createTime;
    private String extend;
}
