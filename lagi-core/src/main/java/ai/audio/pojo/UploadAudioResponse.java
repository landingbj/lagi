package ai.audio.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadAudioResponse {
    private String speaker_id;
    private VolcBaseResp BaseResp;
}
