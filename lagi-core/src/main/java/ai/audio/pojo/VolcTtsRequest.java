package ai.audio.pojo;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VolcTtsRequest {

    private VolcApp app;
    private VolcUser user ;
    private VolcAudio audio;
    private VolcRequest request;

}
