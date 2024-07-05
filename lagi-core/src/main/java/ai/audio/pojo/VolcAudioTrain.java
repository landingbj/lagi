package ai.audio.pojo;

import lombok.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolcAudioTrain {
    private String audio_bytes;
    private String audio_format;
    private String text;
}
