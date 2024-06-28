package ai.audio.pojo;

import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolcAudioTrainRequest  {
    private String appid;
    private String speaker_id;
    private List<VolcAudioTrain> audios;
    private Integer source;
}
