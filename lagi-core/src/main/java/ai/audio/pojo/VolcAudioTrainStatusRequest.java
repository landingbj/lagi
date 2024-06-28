package ai.audio.pojo;


import lombok.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolcAudioTrainStatusRequest {
    private String appid;
    private String speaker_id;
}
