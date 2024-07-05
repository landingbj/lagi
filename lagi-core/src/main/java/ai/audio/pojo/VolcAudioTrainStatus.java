package ai.audio.pojo;


import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VolcAudioTrainStatus {
    private VolcBaseResp BaseResp;
    private String speaker_id;
    private Integer status;
    private String create_time;
    private String version;
    private String demo_audio;
}
