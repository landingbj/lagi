package ai.audio.pojo;

import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VolcAudio {
    private String voice_type = "BV406_V2_streaming";
    private String encoding = "mp3";
    private float speedRatio = 1.0f;
    private float volumeRatio = 10;
    private float pitchRatio = 10;
    private String emotion = "happy";
}
