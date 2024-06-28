package ai.audio.pojo;

import lombok.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioRequest {
    private String model;
    private String appid;
    private String speaker_id;
}
